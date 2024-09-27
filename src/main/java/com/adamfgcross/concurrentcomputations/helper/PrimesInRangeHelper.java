package com.adamfgcross.concurrentcomputations.helper;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.adamfgcross.concurrentcomputations.domain.PrimesInRangeTask;
import com.adamfgcross.concurrentcomputations.domain.PrimesInRangeTaskContext;
import com.adamfgcross.concurrentcomputations.domain.TaskStatus;
import com.adamfgcross.concurrentcomputations.service.PrimesInRangeDataUpdateService;
import com.adamfgcross.concurrentcomputations.service.TaskStoreService;
import com.adamfgcross.concurrentcomputations.task.ComputePrimesInRangeCallable;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@Component
public class PrimesInRangeHelper {
	
	@Value("${spring.concurrency.default.max-thread-time-milliseconds}")
	private Long MAX_THREAD_TIME_IN_MILLISECONDS;
	
	@Value("${spring.concurrency.primes-in-range.interval-per-thread}")
	private Long INTERVAL_PER_THREAD;
	
	@PersistenceContext
    private EntityManager entityManager;
	
	@Autowired
	private PrimesInRangeDataUpdateService primesInRangeDataUpdateService;
	
	@Autowired
	private TaskStoreService<List<String>> primesInRangeTaskStoreService;
	
	@Autowired
	private TaskStoreService<Void> dbUpdateTaskStoreService;
	
	@Autowired
	private TaskExecutorFactory taskExecutorFactory;
	
	private static final Logger logger = LoggerFactory.getLogger(PrimesInRangeHelper.class);
	
	public void computePrimesInRange(PrimesInRangeTaskContext primesInRangeTaskContext) {
		var primesInRangeTask = primesInRangeTaskContext.getPrimesInRangeTask();
		var subranges = getSubranges(primesInRangeTaskContext);
		// get an executor for the domain computations
		ExecutorService executorService = taskExecutorFactory.getTaskExecutor();
		// create a separate executor for database updates
		ExecutorService dbExecutorService = Executors.newFixedThreadPool(1);
		var tasks = generateTasksForSubranges(subranges);
		List<CompletableFuture<List<String>>> computationFutures;
		List<CompletableFuture<Void>> dbUpdateFutures;
		// we synchronize scheduling the tasks and creating and storing their futures
		// to avoid a possible race condition if user attempts to cancel, which is
		// also synchronized on this object
		synchronized (this) {
			var status = primesInRangeTask.getTaskStatus();
			if (status == TaskStatus.CANCELLED) {
				logger.info("found task was cancelled");
				return;
			}
			computationFutures = scheduleTasks(executorService, tasks);
			primesInRangeTaskStoreService.storeTaskFutures(primesInRangeTask.getId(), computationFutures);
			dbUpdateFutures = scheduleAppendingComputedPrimes(primesInRangeTask.getId(), computationFutures, dbExecutorService);
			dbUpdateTaskStoreService.storeTaskFutures(primesInRangeTask.getId(), dbUpdateFutures);
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
		
		dbUpdateFutures.forEach(f -> {
			f.join();
		});
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
	
	private List<CompletableFuture<Void>> scheduleAppendingComputedPrimes(Long taskId, List<CompletableFuture<List<String>>> computationFutures, ExecutorService dbExecutorService) {
		var dbUpdateFutures = new ArrayList<CompletableFuture<Void>>();
		
		// schedule database updates
		computationFutures.forEach(future -> {
			CompletableFuture<Void> dbUpdateFuture = future.thenAcceptAsync(primes -> {
				logger.info("appending computed primes: " + primes.toString());
				primesInRangeDataUpdateService.appendComputedPrimesToResult(taskId, primes);
				return;
			}, dbExecutorService);
			dbUpdateFutures.add(dbUpdateFuture);
		});
		return dbUpdateFutures;
	}
	
	public synchronized void cancelTask(PrimesInRangeTask primesInRangeTask) {
		logger.info("cancelling task:");
		primesInRangeTaskStoreService.getTaskFutures(primesInRangeTask.getId())
		.ifPresent(futures -> {
			logger.info("found " + futures.size() + "domain computation futures");
			futures.forEach(f -> {
				logger.info("cancelling domain future");
				f.cancel(true);
			});
		});
		dbUpdateTaskStoreService.getTaskFutures(primesInRangeTask.getId())
		.ifPresent(futures -> {
			logger.info("found " + futures.size() + "db update futures");
			futures.forEach(f -> {
				logger.info("cancelling db update future");
				f.cancel(true);
			});
		});
		try {
			primesInRangeTaskStoreService.removeTaskFutures(primesInRangeTask.getId());
			dbUpdateTaskStoreService.removeTaskFutures(primesInRangeTask.getId());
			primesInRangeDataUpdateService.markTaskStatusCancelled(primesInRangeTask.getId());
			logger.info("set task status to Cancelled");
		} catch (Exception e) {
			logger.error("an unexpected exception occurred", e);
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
	
	private List<CompletableFuture<List<String>>> scheduleTasks(Executor executor, List<ComputePrimesInRangeCallable> tasks) {
		List<CompletableFuture<List<String>>> futures = new ArrayList<>();
		tasks.forEach(computationTask -> {
			CompletableFuture<List<String>> future = CompletableFuture.supplyAsync(() -> {
				try {
					return computationTask.call();
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
