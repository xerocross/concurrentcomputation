package com.adamfgcross.concurrentcomputations.service;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
	
	private final Executor executor;
	
	private static final Logger logger = LoggerFactory.getLogger(PrimesInRangeService.class);
	
	public PrimesInRangeService(TaskRepository taskRepository,
			PrimesInRangeHelper primesInRangeHelper,
			Executor executor) {
		this.taskRepository = taskRepository;
		this.primesInRangeHelper = primesInRangeHelper;
		this.executor = executor;
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
	
	public Optional<PrimesInRangeResponse> cancelTask(Long id) {
		Optional<Task> taskOptional = taskRepository.findById(id);
		logger.info("attempting to cancel task:");
		taskOptional.ifPresent(task -> {
			if (task instanceof PrimesInRangeTask) {
				primesInRangeHelper.cancelTask( (PrimesInRangeTask) task);
			}
		});
		return taskOptional.map(t -> (PrimesInRangeTask) t).map(this::getTaskResponse);
	}
	
	private PrimesInRangeResponse getTaskResponse(PrimesInRangeTask primesInRangeTask) {
		var taskResponse = new PrimesInRangeResponse(primesInRangeTask);
		return taskResponse;
	}
	
	@Transactional
	public PrimesInRangeTask initiatePrimesInRangeTask(User user, PrimesInRangeRequest primesInRangeRequest) {
		PrimesInRangeTask primesInRangeTask = new PrimesInRangeTask();
		primesInRangeTask.setRangeMax(primesInRangeRequest.getRangeMax());
		primesInRangeTask.setRangeMin(primesInRangeRequest.getRangeMin());
		primesInRangeTask.setUser(user);
		taskRepository.save(primesInRangeTask);
		startComputation(primesInRangeTask);
		return primesInRangeTask;
	}
	
	@Transactional
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
	
	@Transactional
	private CompletableFuture<Void> initiateComputation(PrimesInRangeTaskContext primesInRangeTaskContext) {
		logger.info("initiate computation");
		return CompletableFuture.runAsync(() -> computePrimesInRange(primesInRangeTaskContext), executor);
	}
	
	@Transactional
	private void computePrimesInRange(PrimesInRangeTaskContext primesInRangeTaskContext) {
		primesInRangeHelper.computePrimesInRange(primesInRangeTaskContext);
	}

}
