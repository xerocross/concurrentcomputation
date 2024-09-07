package helper;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.springframework.stereotype.Component;

import com.adamfgcross.concurrentcomputations.domain.PrimesInRangeTask;
import com.adamfgcross.concurrentcomputations.domain.PrimesInRangeTaskContext;
import com.adamfgcross.concurrentcomputations.service.PrimesInRangeService;
import com.adamfgcross.concurrentcomputations.task.ComputePrimesInRangeCallable;

@Component
public class PrimesInRangeHelper {
	
	private PrimesInRangeService primesInRangeService;
	
	public PrimesInRangeHelper(PrimesInRangeService primesInRangeService) {
		this.primesInRangeService = primesInRangeService;
	}
	
	public PrimesInRangeTaskContext computePrimesInRange(PrimesInRangeTaskContext primesInRangeTaskContext) {
		int numThreads = 3;
		PrimesInRangeTask primesInRangeTask = primesInRangeTaskContext.getPrimesInRangeTask();
		var subranges = getSubranges(primesInRangeTaskContext);
		ExecutorService executorService = Executors.newFixedThreadPool(numThreads);
		List<ComputePrimesInRangeCallable> tasks = new ArrayList<>();
		List<CompletableFuture<List<String>>> futures = new ArrayList<>();
		subranges.forEach(subrange -> {
			tasks.add(new ComputePrimesInRangeCallable(subrange.getMin(), subrange.getMax()));
		});
		
		tasks.forEach(computationTask -> {
			CompletableFuture<List<String>> future = CompletableFuture.supplyAsync(() -> {
				try {
					return computationTask.call();
				} catch (Exception e) {
					e.printStackTrace();
					return null;
				}
			}, executorService);
			future.thenAccept(primes -> {
				primesInRangeService.appendComputedPrimesToResult(primesInRangeTask, primes);
			});
			futures.add(future);
		});
		CompletableFuture<Void> allDone = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
        allDone.join();  // Wait for all tasks


        // Shutdown the ExecutorService
        executorService.shutdown();
		return primesInRangeTaskContext;
	}
	
	private List<SubRange> getSubranges(PrimesInRangeTaskContext primesInRangeTaskContext) {
		Long rangeMin = Long.parseLong(primesInRangeTaskContext.getRangeMin());
		Long rangeMax = Long.parseLong(primesInRangeTaskContext.getRangeMax());
		Long range = rangeMax - rangeMin;
		List<SubRange> subranges = new ArrayList<>();
		Long intervalPerThread = 1000L;
		Long numWholeIntervals = range / intervalPerThread;
		Long remainder = range % intervalPerThread;
		
		for (Long i = 0L; i < numWholeIntervals; i++) {
			subranges.add(new SubRange(rangeMin + i*intervalPerThread, rangeMin + (i + 1)*intervalPerThread));
		}
		if (remainder > 0L) {
			subranges.add(new SubRange(rangeMin + (numWholeIntervals - 1 )*intervalPerThread, rangeMax));
		}
		return subranges;
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
