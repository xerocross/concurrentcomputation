package com.adamfgcross.concurrentcomputations.service;

import java.util.concurrent.CompletableFuture;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.adamfgcross.concurrentcomputations.domain.PrimesInRangeTask;
import com.adamfgcross.concurrentcomputations.domain.PrimesInRangeTaskContext;
import com.adamfgcross.concurrentcomputations.domain.User;
import com.adamfgcross.concurrentcomputations.dto.PrimesInRangeRequest;
import com.adamfgcross.concurrentcomputations.repository.TaskRepository;

@Service
public class PrimesInRangeService {
	
	private TaskRepository taskRepository;
	
	public PrimesInRangeService(TaskRepository taskRepository) {
		this.taskRepository = taskRepository;
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
	private CompletableFuture<PrimesInRangeTaskContext> computePrimesInRange(PrimesInRangeTaskContext primesInRangeTaskContext) {
		return null;
	}
}
