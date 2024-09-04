package com.adamfgcross.concurrentcomputations.service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.adamfgcross.concurrentcomputations.domain.IntFactorTask;
import com.adamfgcross.concurrentcomputations.domain.IntFactorTaskContext;
import com.adamfgcross.concurrentcomputations.dto.IntFactorTaskRequest;
import com.adamfgcross.concurrentcomputations.repository.TaskRepository;

@Service
public class IntFactorService implements TaskService<IntFactorTaskRequest> {
	
	private TaskRepository taskRepository;
	
	public IntFactorService(TaskRepository taskRepository) {
		this.taskRepository = taskRepository;
	}
	
	public Long submitTask(IntFactorTaskRequest intFactorTaskRequest) {
		IntFactorTask intFactorTask = new IntFactorTask();
		intFactorTask.setIntegerForFactoring(intFactorTaskRequest.getIntToFactor());
		taskRepository.save(intFactorTask);
		beginTask(intFactorTask);
		return intFactorTask.getId();
	}
	
	
	private void beginTask(IntFactorTask intFactorTask) {
		IntFactorTaskContext intFactorTaskContext = new IntFactorTaskContext(intFactorTask);
		CompletableFuture<IntFactorTaskContext> completableFuture = computePrimeFactorization(intFactorTaskContext);
		completableFuture
			.thenAccept(this::updateTaskFinished);
		
	}
	
	@Async
	private CompletableFuture<IntFactorTaskContext> computePrimeFactorization(IntFactorTaskContext intFactorTaskContext) {
		return CompletableFuture.supplyAsync(() -> getPrimeFactorization(intFactorTaskContext));
	}
	
	
	private void updateTaskFinished(IntFactorTaskContext intFactorTaskContext) {
		IntFactorTask task = intFactorTaskContext.getIntFactorTask();
		task.setFactors(intFactorTaskContext
				.getFactors().stream().map(x -> x.toString()).toList());
		intFactorTaskContext.getIntFactorTask().setIsCompleted(true);
	}
	
	private IntFactorTaskContext getPrimeFactorization(IntFactorTaskContext intFactorTaskContext) {
		Long inputInteger = intFactorTaskContext.getNumber();
		List<Long> factors = new ArrayList<>();
        for (long i = 2; i <= inputInteger / i; i++) {
            while (inputInteger % i == 0) {
                factors.add(i);
                inputInteger /= i;
            }
        }
        if (inputInteger > 1) {
            factors.add(inputInteger);
        }
        intFactorTaskContext.setFactors(factors);
        return intFactorTaskContext;
	}
}
