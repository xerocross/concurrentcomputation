package com.adamfgcross.concurrentcomputations.service;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.adamfgcross.concurrentcomputations.domain.PrimesInRangeTask;
import com.adamfgcross.concurrentcomputations.domain.PrimesInRangeTaskContext;
import com.adamfgcross.concurrentcomputations.domain.Task;
import com.adamfgcross.concurrentcomputations.domain.User;
import com.adamfgcross.concurrentcomputations.dto.PrimesInRangeRequest;
import com.adamfgcross.concurrentcomputations.dto.PrimesInRangeResponse;
import com.adamfgcross.concurrentcomputations.helper.PrimesInRangeHelper;
import com.adamfgcross.concurrentcomputations.repository.TaskRepository;

@Service
public class PrimesInRangeService {
	
	private TaskRepository taskRepository;
	private PrimesInRangeHelper primesInRangeHelper;
	private static final Logger logger = LoggerFactory.getLogger(PrimesInRangeService.class);
	
	public PrimesInRangeService(TaskRepository taskRepository,
			PrimesInRangeHelper primesInRangeHelper) {
		this.taskRepository = taskRepository;
		this.primesInRangeHelper = primesInRangeHelper;
	}
	
	public Optional<PrimesInRangeResponse> getTask(Long id) {
		Optional<Task> taskOptional = taskRepository.findById(id);
		if (taskOptional.isPresent()) {
			Task task = taskOptional.get();
			if (task instanceof PrimesInRangeTask) {
				return Optional.of(getTaskResponse((PrimesInRangeTask) task));
			} else {
				return Optional.empty();
			}
		} else {
			return Optional.empty();
		}
	}
	
	private PrimesInRangeResponse getTaskResponse(PrimesInRangeTask primesInRangeTask) {
		var taskResponse = new PrimesInRangeResponse(primesInRangeTask);
		return taskResponse;
	}
	
	public PrimesInRangeTask initiatePrimesInRangeTask(User user, PrimesInRangeRequest primesInRangeRequest) {
		PrimesInRangeTask primesInRangeTask = new PrimesInRangeTask();
		primesInRangeTask.setRangeMax(primesInRangeRequest.getRangeMax());
		primesInRangeTask.setRangeMin(primesInRangeRequest.getRangeMin());
		primesInRangeTask.setUser(user);
		taskRepository.save(primesInRangeTask);
		startComputation(primesInRangeTask);
		return primesInRangeTask;
	}
	
	private void startComputation(PrimesInRangeTask primesInRangeTask) {
		PrimesInRangeTaskContext primesInRangeTaskContext = new PrimesInRangeTaskContext();
		primesInRangeTaskContext.setRangeMax(primesInRangeTask.getRangeMax());
		primesInRangeTaskContext.setRangeMin(primesInRangeTask.getRangeMin());
		primesInRangeTaskContext.setPrimesInRangeTask(primesInRangeTask);
		initiateComputation(primesInRangeTaskContext)
			.thenRun(() -> {
				logger.info("finished computing primes");
			});
	}

	synchronized void markComplete(PrimesInRangeTaskContext primesInRangeTaskContext) {
		var task = primesInRangeTaskContext.getPrimesInRangeTask();
		task.setIsCompleted(true);
		taskRepository.save(task);
	}
	
	@Async
	private CompletableFuture<PrimesInRangeTaskContext> initiateComputation(PrimesInRangeTaskContext primesInRangeTaskContext) {
		return CompletableFuture.supplyAsync(() -> computePrimesInRange(primesInRangeTaskContext));
	}
	
	private PrimesInRangeTaskContext computePrimesInRange(PrimesInRangeTaskContext primesInRangeTaskContext) {
		return primesInRangeHelper.computePrimesInRange(primesInRangeTaskContext);
	}
	
	
	synchronized void appendComputedPrimesToResult(PrimesInRangeTask primesInRangeTask, List<String> primes) {
		primesInRangeTask.getPrimes().addAll(primes);
		taskRepository.save(primesInRangeTask);
	}
}
