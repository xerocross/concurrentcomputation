package com.adamfgcross.concurrentcomputations.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.adamfgcross.concurrentcomputations.domain.PrimesInRangeTask;
import com.adamfgcross.concurrentcomputations.domain.TaskStatus;
import com.adamfgcross.concurrentcomputations.repository.TaskRepository;

@Service
public class PrimesInRangeDataUpdateService {
	
	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	TaskRepository taskRepository;
	
	@Transactional
	public synchronized void appendComputedPrimesToResult(Long taskId, List<String> primes) {
		taskRepository.findById(taskId).map(task -> (PrimesInRangeTask) task)
			.ifPresent(task -> {
				logger.info("appendComputedPrimesToResult is called");
				try {
					var currentPrimes = task.getPrimes();
					currentPrimes.addAll(primes);
					taskRepository.save(task);
				} catch (Exception e) {
					logger.error("encountered an error while saving primes:", e);
				}
			});
	}
	
	@Transactional
	public synchronized void markTaskComplete(Long taskId) {
		taskRepository.findById(taskId).map(task -> (PrimesInRangeTask) task)
		.ifPresent(task -> {
			task.setIsCompleted(true);
			task.setTaskStatus(TaskStatus.COMPLETE);
			taskRepository.save(task);
		});
	}
	
	@Transactional
	public synchronized void markTaskStatusError(Long taskId) {
		taskRepository.findById(taskId).map(task -> (PrimesInRangeTask) task)
		.ifPresent(task -> {
			task.setTaskStatus(TaskStatus.ERROR);
			taskRepository.save(task);
		});
	}
	
	@Transactional
	public synchronized void markTaskStatusCancelled(Long taskId) {
		taskRepository.findById(taskId).map(task -> (PrimesInRangeTask) task)
		.ifPresent(task -> {
			task.setTaskStatus(TaskStatus.CANCELLED);
			taskRepository.save(task);
		});
	}
}
