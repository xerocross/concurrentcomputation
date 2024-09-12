package com.adamfgcross.concurrentcomputations.helper;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.adamfgcross.concurrentcomputations.domain.PrimesInRangeTask;
import com.adamfgcross.concurrentcomputations.domain.PrimesInRangeTaskContext;
import com.adamfgcross.concurrentcomputations.repository.TaskRepository;
import com.adamfgcross.concurrentcomputations.service.TaskStoreService;
import com.adamfgcross.concurrentcomputations.task.ComputePrimesInRangeCallable;

import jakarta.transaction.Transactional;

@Component
public class PrimesInRangeHelper {
	
	@Autowired
	private TaskRepository taskRepository;
	
	@Autowired
	private TaskStoreService taskStoreService;
	
	private static final Logger logger = LoggerFactory.getLogger(PrimesInRangeHelper.class);
	
	public PrimesInRangeTaskContext computePrimesInRange(PrimesInRangeTaskContext primesInRangeTaskContext) {
		int numThreads = 3;
		PrimesInRangeTask primesInRangeTask = primesInRangeTaskContext.getPrimesInRangeTask();
		var subranges = getSubranges(primesInRangeTaskContext);
		ExecutorService executorService = Executors.newFixedThreadPool(numThreads);
		List<ComputePrimesInRangeCallable> tasks = new ArrayList<>();
		List<CompletableFuture<List<String>>> futures = new ArrayList<>();
		subranges.forEach(subrange -> {
			logger.info("generating thread for subrange: " + subrange.getMin() + " to " + subrange.getMax());
			tasks.add(new ComputePrimesInRangeCallable(subrange.getMin(), subrange.getMax()));
		});
		
		tasks.forEach(computationTask -> {
			CompletableFuture<List<String>> future = CompletableFuture.supplyAsync(() -> {
				try {
					return computationTask.call();
				} catch (Exception e) {
					e.printStackTrace();
					logger.error("task encountered an exception", e);
					return null;
				}
			}, executorService);
			future.thenAccept(primes -> {
				logger.info("appending computed primes: " + primes.toString());
				appendComputedPrimesToResult(primesInRangeTask, primes);
			});
			futures.add(future);
		});
		futures.forEach(future -> {
			taskStoreService.storeTaskFuture(primesInRangeTask.getId(), future);
		});
		CompletableFuture<Void> allDone = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
		allDone.join();
		markTaskComplete(primesInRangeTask);
		logger.info("finished computing primes in range");
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
		return primesInRangeTaskContext;
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
		taskRepository.save(primesInRangeTask);
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
