package com.adamfgcross.concurrentcomputations.service;

import org.springframework.stereotype.Service;

import com.adamfgcross.concurrentcomputations.domain.IntFactorTask;
import com.adamfgcross.concurrentcomputations.dto.IntFactorTaskRequest;
import com.adamfgcross.concurrentcomputations.repository.TaskRepository;

@Service
public class IntFactorService {
	
	private TaskRepository taskRepository;
	
	public IntFactorService(TaskRepository taskRepository) {
		this.taskRepository = taskRepository;
	}
	
	public Long submitTask(IntFactorTaskRequest intFactorTaskRequest) {
		IntFactorTask intFactorTask = new IntFactorTask();
		intFactorTask.setIntegerForFactoring(intFactorTaskRequest.getIntToFactor());
		taskRepository.save(intFactorTask);
		return intFactorTask.getId();
	}
}
