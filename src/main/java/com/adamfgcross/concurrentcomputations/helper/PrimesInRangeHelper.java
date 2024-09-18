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
import com.adamfgcross.concurrentcomputations.repository.TaskRepository;
import com.adamfgcross.concurrentcomputations.service.TaskStoreService;
import com.adamfgcross.concurrentcomputations.task.ComputePrimesInRangeCallable;

import jakarta.transaction.Transactional;

@Component
public class PrimesInRangeHelper {
	
	@Value("${spring.concurrency.max-thread-time-milliseconds}")
	private Long MAX_THREAD_TIME_IN_MILLISECONDS;
	
	@Autowired
	private TaskRepository taskRepository;
	
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
		var computationFutures = scheduleTasks(executorService, tasks);
		primesInRangeTaskStoreService.storeTaskFutures(primesInRangeTask.getId(), computationFutures);
		
		
		var dbUpdateFutures = new ArrayList<CompletableFuture<Void>>();
		
		// schedule database updates
		computationFutures.forEach(future -> {
			CompletableFuture<Void> dbUpdateFuture = future.thenAcceptAsync(primes -> {
				logger.info("appending computed primes: " + primes.toString());
				appendComputedPrimesToResult(primesInRangeTask, primes);
				return;
			}, dbExecutorService);
			dbUpdateFutures.add(dbUpdateFuture);
		});
		List<CompletableFuture<?>> incompleteComputationFutures = new ArrayList<>();
		computationFutures.forEach(f -> {
			try {
				f.get(MAX_THREAD_TIME_IN_MILLISECONDS, TimeUnit.MILLISECONDS);
			} catch (TimeoutException e) {
			    logger.info("thread timeout occurred");
			    markTaskStatusError(primesInRangeTask);
			    incompleteComputationFutures.add(f);
			} catch (InterruptedException | ExecutionException e) {
				markTaskStatusError(primesInRangeTask);
				e.printStackTrace();
				logger.error("An exception occurred in domain computation thread.", e);
				incompleteComputationFutures.add(f);
			} catch (CancellationException e) {
				logger.info("thread was cancelled");
				incompleteComputationFutures.add(f);
			}
		});
		dbUpdateTaskStoreService.storeTaskFutures(primesInRangeTask.getId(), dbUpdateFutures);
		dbUpdateFutures.forEach(f -> {
			f.join();
		});
		if (incompleteComputationFutures.isEmpty()) {
			markTaskComplete(primesInRangeTask);
		}
		logger.info("finished computing primes in range");
		shutdownExecutor(executorService);
		dbExecutorService.shutdown();
		primesInRangeTaskStoreService.removeTaskFutures(primesInRangeTask.getId());
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
		Long intervalPerThread = 1000L;
		Long numWholeIntervals = range / intervalPerThread;
		Long remainder = range % intervalPerThread;
		
		for (Long i = 0L; i < numWholeIntervals; i++) {
			subranges.add(new SubRange(rangeMin + i*intervalPerThread, rangeMin + (i + 1)*intervalPerThread));
		}
		if (remainder > 0L) {
			subranges.add(new SubRange(rangeMin + (numWholeIntervals)*intervalPerThread, rangeMax));
		}
		return subranges;
	}
	
	private void storeFutures(Long taskId, List<CompletableFuture<?>> futures) {
		futures.forEach(future -> {
			taskStoreService.storeTaskFuture(taskId, future);
		});
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
		});
		return futures;
	}
	
	@Transactional
	private synchronized void appendComputedPrimesToResult(PrimesInRangeTask primesInRangeTask, List<String> primes) {
		logger.info("appendComputedPrimesToResult is called");
		try {
			var currentPrimes = primesInRangeTask.getPrimes();
			logger.info("found that primes is currently: " + currentPrimes.toString());
			currentPrimes.addAll(primes);
			logger.info("added primes: " + primes.toString());
			taskRepository.save(primesInRangeTask);
		} catch (Exception e) {
			logger.error("encountered an error while saving primes:", e);
		}
	}
	
	@Transactional
	private synchronized void markTaskComplete(PrimesInRangeTask primesInRangeTask) {
		primesInRangeTask.setIsCompleted(true);
		primesInRangeTask.setTaskStatus(TaskStatus.COMPLETE);
		taskRepository.save(primesInRangeTask);
	}
	
	@Transactional
	private synchronized void markTaskStatusError(PrimesInRangeTask primesInRangeTask) {
		primesInRangeTask.setTaskStatus(TaskStatus.ERROR);
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
