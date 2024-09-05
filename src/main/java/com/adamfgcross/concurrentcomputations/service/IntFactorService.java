package com.adamfgcross.concurrentcomputations.service;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.adamfgcross.concurrentcomputations.domain.IntFactorTask;
import com.adamfgcross.concurrentcomputations.domain.IntFactorTaskContext;
import com.adamfgcross.concurrentcomputations.domain.Task;
import com.adamfgcross.concurrentcomputations.dto.IntFactorTaskRequest;
import com.adamfgcross.concurrentcomputations.dto.IntFactorTaskResponse;
import com.adamfgcross.concurrentcomputations.repository.TaskRepository;

@Service
public class IntFactorService implements TaskService<IntFactorTaskRequest> {
	
	private TaskRepository taskRepository;
	
	private static final Logger logger = LoggerFactory.getLogger(IntFactorService.class);
	
	public IntFactorService(TaskRepository taskRepository) {
		this.taskRepository = taskRepository;
	}
	
	public Long submitTask(IntFactorTaskRequest intFactorTaskRequest) {
		IntFactorTask intFactorTask = new IntFactorTask();
		intFactorTask.setNumber(intFactorTaskRequest.getIntToFactor());
		taskRepository.save(intFactorTask);
		beginTask(intFactorTask);
		return intFactorTask.getId();
	}
	
	public Optional<IntFactorTaskResponse> getTask(Long id) {
		Optional<Task> taskOptional = taskRepository.findById(id);
		if (taskOptional.isPresent()) {
			Task task = taskOptional.get();
			if (task instanceof IntFactorTask) {
				return Optional.of(getTaskResponse((IntFactorTask) task));
			} else {
				return Optional.empty();
			}
		} else {
			return Optional.empty();
		}
	}
	
	private IntFactorTaskResponse getTaskResponse(IntFactorTask intFactorTask) {
		IntFactorTaskResponse intFactorTaskResponse = new IntFactorTaskResponse();
		intFactorTaskResponse.setId(intFactorTask.getId());
		intFactorTaskResponse.setNumber(intFactorTask.getNumber());
		intFactorTaskResponse.setFactors(intFactorTask.getFactors());
		intFactorTaskResponse.setIsCompleted(intFactorTask.getIsCompleted());
		return intFactorTaskResponse;
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
		logger.info("setting task completed");
		IntFactorTask task = intFactorTaskContext.getIntFactorTask();
		task.setFactors(intFactorTaskContext
				.getFactors().stream().map(x -> x.toString()).toList());
		intFactorTaskContext.getIntFactorTask().setIsCompleted(true);
		taskRepository.save(task);
	}
	
	private IntFactorTaskContext getPrimeFactorization(IntFactorTaskContext intFactorTaskContext) {
		String inputInteger = intFactorTaskContext.getNumber();
		logger.info("starting to factor " + inputInteger);
		BigInteger number = new BigInteger(inputInteger);
		
		List<String> factors = new ArrayList<>();
		for (BigInteger i = BigInteger.valueOf(2); i.compareTo(number.divide(i)) <= 0; i = i.add(BigInteger.valueOf(1))) {
			logger.info("testing for factor at: " + i.toString());
			while (number.mod(i).equals(BigInteger.valueOf(0)) ) {
            	logger.info("found factor: " + i.toString());
                factors.add(i.toString());
                number = number.divide(i);
            }
        }
		if (number.compareTo(BigInteger.valueOf(1)) > 0) {
			logger.info("found factor: " + number.toString());
            factors.add(number.toString());
        }
        intFactorTaskContext.setFactors(factors);
        logger.info("finished factoring " + inputInteger);
        return intFactorTaskContext;
	}
}
