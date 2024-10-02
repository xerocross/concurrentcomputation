package com.adamfgcross.concurrentcomputations.helper;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.adamfgcross.concurrentcomputations.domain.PrimesInRangeTask;
import com.adamfgcross.concurrentcomputations.domain.PrimesInRangeTaskContext;
import com.adamfgcross.concurrentcomputations.domain.TaskStatus;
import com.adamfgcross.concurrentcomputations.service.PrimesInRangeDataUpdateService;
import com.adamfgcross.concurrentcomputations.service.PrimesInRangeWorkUpdate;
import com.adamfgcross.concurrentcomputations.service.TaskStoreService;
import com.adamfgcross.concurrentcomputations.task.ComputePrimesInRangeCallable;


@Component
public class PrimesInRangeHelper {
	
	@Value("${spring.concurrency.default.max-thread-time-milliseconds}")
	private Long MAX_THREAD_TIME_IN_MILLISECONDS;
	
	private ReentrantLock cancelingLock = new ReentrantLock();
	
	@Value("${spring.concurrency.primes-in-range.interval-per-thread}")
	private Long INTERVAL_PER_THREAD;
	

	@Autowired
	private PrimesInRangeDataUpdateService primesInRangeDataUpdateService;
	
	@Autowired
	private TaskStoreService<List<String>> primesInRangeTaskStoreService;
	
	@Autowired
	private TaskStoreService<Void> dbUpdateTaskStoreService;
	
	@Autowired
	private TaskExecutorFactory taskExecutorFactory;
	
	private static final Logger logger = LoggerFactory.getLogger(PrimesInRangeHelper.class);
	
	
	private static class DBUpdaterThread extends Thread {
		
		private Long taskId;
		private BlockingQueue<PrimesInRangeWorkUpdate> workUpdateQueue;
		private PrimesInRangeDataUpdateService primesInRangeDataUpdateService;
		private Logger logger = LoggerFactory.getLogger(this.getClass());
		private volatile boolean running = true;
		
		public DBUpdaterThread(Long taskId, 
				BlockingQueue<PrimesInRangeWorkUpdate> workUpdateQueue,
				PrimesInRangeDataUpdateService primesInRangeDataUpdateService) {
			this.taskId = taskId;
			this.workUpdateQueue = workUpdateQueue;
			this.primesInRangeDataUpdateService = primesInRangeDataUpdateService;
		}
		
		public void terminate() {
			logger.info("stopping DB update thread for task " + taskId);
			this.running = false;
		}

		public void run() {
			while (running) {
				try {
					processQueue();
				} catch (InterruptedException e) {
					continue;
				}
				
			}
		}
		
		private void processQueue () throws InterruptedException {
			// pull work from the queue
			// if the queue is empty, wait
			var workUpdate = workUpdateQueue.take();
			var availableUpdates = new ArrayList<PrimesInRangeWorkUpdate>();
			availableUpdates.add(workUpdate);
			// if there are more workUpdates already waiting, pull
			// them too
			while ((workUpdate = workUpdateQueue.poll()) != null) {
				availableUpdates.add(workUpdate);
			}
			// batch work
			Set<String> primes = new HashSet<>();
			
			availableUpdates.forEach(w -> {
				primes.addAll(w.getPrimes());
			});
			var isTerminal = false;
			if (availableUpdates.stream().anyMatch(PrimesInRangeWorkUpdate::isTerminal)) {
				isTerminal = true;
			}
			
			// update database with new primes
			logger.info("posting primes to database for task: " + this.taskId);
			primesInRangeDataUpdateService.appendComputedPrimesToResult(taskId, new ArrayList<>(primes));
			if (isTerminal) {
				this.terminate();
			}
		}
	}
	
	public void computePrimesInRange(PrimesInRangeTaskContext primesInRangeTaskContext) {
		var primesInRangeTask = primesInRangeTaskContext.getPrimesInRangeTask();
		var subranges = getSubranges(primesInRangeTaskContext);
		// get an executor for the domain computations
		ExecutorService executorService = taskExecutorFactory.getTaskExecutor();
		// create a separate executor for database updates
		ExecutorService dbExecutorService = Executors.newFixedThreadPool(1);
		var tasks = generateTasksForSubranges(subranges);
		List<CompletableFuture<List<String>>> computationFutures;
		// List<CompletableFuture<Void>> dbUpdateFutures;
		var taskId = primesInRangeTask.getId();
		CountDownLatch latch = new CountDownLatch(tasks.size());
		
		BlockingQueue<PrimesInRangeWorkUpdate> workUpdateQueue = new LinkedBlockingQueue<>();
		DBUpdaterThread dbUpdaterThread = new DBUpdaterThread(taskId, workUpdateQueue, primesInRangeDataUpdateService);
		
		// we synchronize scheduling the tasks and creating and storing their futures
		// to avoid a possible race condition that may occur if the user cancels
		// the task: canceling while scheduling is in progress would potentially
		// result in incomplete cancellation.
		cancelingLock.lock();
		try {
			var status = primesInRangeTask.getTaskStatus();
			if (status == TaskStatus.CANCELLED) {
				logger.info("found task %d was cancelled before computation was scheduled; will not begin computation.", taskId);
				return;
			}
			computationFutures = scheduleTasks(executorService, tasks, workUpdateQueue, latch);
			primesInRangeDataUpdateService.markTaskStatusScheduled(taskId);
			primesInRangeTaskStoreService.storeTaskFutures(taskId, computationFutures);
			dbUpdaterThread.start();
			// dbUpdateFutures = scheduleAppendingComputedPrimes(taskId, computationFutures, dbExecutorService);
			
			// dbUpdateTaskStoreService.storeTaskFutures(taskId, dbUpdateFutures);
		} finally {
			cancelingLock.unlock();
		}

		
		List<CompletableFuture<?>> incompleteComputationFutures = new ArrayList<>();
		computationFutures.forEach(f -> {
			try {
				f.get(MAX_THREAD_TIME_IN_MILLISECONDS, TimeUnit.MILLISECONDS);
			} catch (TimeoutException e) {
			    logger.info("thread timeout occurred");
			    CompletableFuture.runAsync(() -> {
			    	primesInRangeDataUpdateService.markTaskStatusError(primesInRangeTask.getId());
			    }, dbExecutorService);
			    incompleteComputationFutures.add(f);
			} catch (InterruptedException | ExecutionException e) {
				CompletableFuture.runAsync(() -> {
					primesInRangeDataUpdateService.markTaskStatusError(primesInRangeTask.getId());
			    }, dbExecutorService);
				e.printStackTrace();
				logger.error("An exception occurred in domain computation thread.", e);
				incompleteComputationFutures.add(f);
			} catch (CancellationException e) {
				CompletableFuture.runAsync(() -> {
					primesInRangeDataUpdateService.markTaskStatusCancelled(primesInRangeTask.getId());
			    	logger.info("thread was cancelled");
			    }, dbExecutorService);
				incompleteComputationFutures.add(f);
			}
		});
		
		try {
			workUpdateQueue.put(PrimesInRangeWorkUpdate.getTerminal());
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
//		dbUpdateFutures.forEach(f -> {
//			f.join();
//		});
		if (incompleteComputationFutures.isEmpty()) {
			CompletableFuture.runAsync(() -> {
				primesInRangeDataUpdateService.markTaskComplete(primesInRangeTask.getId());
		    }, dbExecutorService);
		}
		logger.info("finished computing primes in range");
		shutdownExecutor(executorService);
		dbExecutorService.shutdown();
		primesInRangeTaskStoreService.removeTaskFutures(primesInRangeTask.getId());
		dbUpdateTaskStoreService.removeTaskFutures(primesInRangeTask.getId());
	}
	
//	private List<CompletableFuture<Void>> scheduleAppendingComputedPrimes(Long taskId, List<CompletableFuture<List<String>>> computationFutures, ExecutorService dbExecutorService) {
//		var dbUpdateFutures = new ArrayList<CompletableFuture<Void>>();
//		
//		// schedule database updates
//		computationFutures.forEach(future -> {
//			CompletableFuture<Void> dbUpdateFuture = future.thenAcceptAsync(primes -> {
//				primesInRangeDataUpdateService.appendComputedPrimesToResult(taskId, primes);
//				return;
//			}, dbExecutorService);
//			dbUpdateFutures.add(dbUpdateFuture);
//		});
//		return dbUpdateFutures;
//	}
//	
	public void cancelTask(PrimesInRangeTask primesInRangeTask) {
		cancelingLock.lock();
		
		try {
			logger.info("cancelling task:");
			// cancel computation task futures
			primesInRangeTaskStoreService.getTaskFutures(primesInRangeTask.getId())
			.ifPresent(futures -> {
				logger.info("found " + futures.size() + "domain computation futures");
				futures.forEach(f -> {
					logger.info("cancelling domain future");
					f.cancel(true);
				});
			});
			// cancel database update futures
			dbUpdateTaskStoreService.getTaskFutures(primesInRangeTask.getId())
			.ifPresent(futures -> {
				logger.info("found " + futures.size() + "db update futures");
				futures.forEach(f -> {
					logger.info("cancelling db update future");
					f.cancel(true);
				});
			});
			// remove the futures so they can be garbage-collected
			primesInRangeTaskStoreService.removeTaskFutures(primesInRangeTask.getId());
			dbUpdateTaskStoreService.removeTaskFutures(primesInRangeTask.getId());
			primesInRangeDataUpdateService.markTaskStatusCancelled(primesInRangeTask.getId());
			logger.info("task %d was successfully cancelled", primesInRangeTask.getId());
		} finally {
			cancelingLock.unlock();
		}
	}
	
	private void shutdownExecutor(ExecutorService executorService) {
		executorService.shutdown();
        try {
            if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
                logger.warn("Executor did not terminate in the specified time.");
                executorService.shutdownNow();  // Force shutdown if not completed
            }
        } catch (InterruptedException ex) {
            logger.error("Executor termination interrupted.", ex);
            executorService.shutdownNow();  // Force shutdown on interruption
            Thread.currentThread().interrupt();
        }
	}
	
	private List<SubRange> getSubranges(PrimesInRangeTaskContext primesInRangeTaskContext) {
		Long rangeMin = primesInRangeTaskContext.getRangeMin();
		Long rangeMax = primesInRangeTaskContext.getRangeMax();
		Long range = rangeMax - rangeMin;
		List<SubRange> subranges = new ArrayList<>();
		Long numWholeIntervals = range / INTERVAL_PER_THREAD;
		Long remainder = range % INTERVAL_PER_THREAD;
		
		for (Long i = 0L; i < numWholeIntervals; i++) {
			subranges.add(new SubRange(rangeMin + i*INTERVAL_PER_THREAD, rangeMin + (i + 1)*INTERVAL_PER_THREAD));
		}
		if (remainder > 0L) {
			subranges.add(new SubRange(rangeMin + (numWholeIntervals)*INTERVAL_PER_THREAD, rangeMax));
		}
		return subranges;
	}
	
	private List<ComputePrimesInRangeCallable> generateTasksForSubranges(List<SubRange> subranges) {
		List<ComputePrimesInRangeCallable> tasks = new ArrayList<>();
		subranges.forEach(subrange -> {
			logger.info("generating thread for subrange: " + subrange.getMin() + " to " + subrange.getMax());
			tasks.add(new ComputePrimesInRangeCallable(subrange.getMin(), subrange.getMax()));
		});
		return tasks;
	}
	
	private List<CompletableFuture<List<String>>> scheduleTasks(Executor executor, 
			List<ComputePrimesInRangeCallable> tasks,
			BlockingQueue<PrimesInRangeWorkUpdate> workUpdateQueue,
			CountDownLatch latch) {
		List<CompletableFuture<List<String>>> futures = new ArrayList<>();
		tasks.forEach(computationTask -> {
			CompletableFuture<List<String>> future = CompletableFuture.supplyAsync(() -> {
				try {
					List<String> primes = computationTask.call();
					// push primes to work queue
					workUpdateQueue.put(new PrimesInRangeWorkUpdate(new HashSet<>(primes)));
					latch.countDown();
					return primes;
					
				} catch (Exception e) {
					e.printStackTrace();
					logger.error("task encountered an exception", e);
					return null;
				}
			}, executor);
			futures.add(future);
			logger.info("task scheduled and future created");
		});
		return futures;
	}
}

class SubRange {
	private Long min;
	public Long getMin() {
		return min;
	}

	public Long getMax() {
		return max;
	}

	private Long max;
	
	public SubRange(Long min, Long max) {
		this.min = min;
		this.max = max;
	}
}
