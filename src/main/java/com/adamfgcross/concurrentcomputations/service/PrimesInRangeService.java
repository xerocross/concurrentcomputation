package com.adamfgcross.concurrentcomputations.service;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.adamfgcross.concurrentcomputations.domain.PrimesInRangeTask;
import com.adamfgcross.concurrentcomputations.domain.PrimesInRangeTaskContext;
import com.adamfgcross.concurrentcomputations.domain.User;
import com.adamfgcross.concurrentcomputations.dto.PrimesInRangeRequest;
import com.adamfgcross.concurrentcomputations.repository.TaskRepository;

import helper.PrimesInRangeHelper;

@Service
public class PrimesInRangeService {
	
	private TaskRepository taskRepository;
	private PrimesInRangeHelper primesInRangeHelper;
	
	public PrimesInRangeService(TaskRepository taskRepository,
			PrimesInRangeHelper primesInRangeHelper) {
		this.taskRepository = taskRepository;
		this.primesInRangeHelper = primesInRangeHelper;
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
	}

	@Async
	private CompletableFuture<PrimesInRangeTaskContext> initiateComputation(PrimesInRangeTaskContext primesInRangeTaskContext) {
		return CompletableFuture.supplyAsync(() -> computePrimesInRange(primesInRangeTaskContext));
	}
	
	private PrimesInRangeTaskContext computePrimesInRange(PrimesInRangeTaskContext primesInRangeTaskContext) {
		return primesInRangeHelper.computePrimesInRange(primesInRangeTaskContext);
	}
	
	
	public synchronized void appendComputedPrimesToResult(PrimesInRangeTask primesInRangeTask, List<String> primes) {
		primesInRangeTask.getPrimes().addAll(primes);
		taskRepository.save(primesInRangeTask);
	}
}
