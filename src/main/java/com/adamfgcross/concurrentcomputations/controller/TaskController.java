package com.adamfgcross.concurrentcomputations.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.adamfgcross.concurrentcomputations.dto.IntFactorTaskRequest;
import com.adamfgcross.concurrentcomputations.dto.TaskRequest;
import com.adamfgcross.concurrentcomputations.dto.TaskResponse;
import com.adamfgcross.concurrentcomputations.service.IntFactorService;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

	private IntFactorService intFactorService;
	
	public TaskController(IntFactorService intFactorService) {
		this.intFactorService = intFactorService;
	}
	
	
	@PostMapping
	public ResponseEntity<TaskResponse> submitTask(@RequestBody TaskRequest taskRequest) {
		if (taskRequest instanceof IntFactorTaskRequest) {
			Long taskId = intFactorService.submitTask((IntFactorTaskRequest) taskRequest);
			TaskResponse taskResponse = new TaskResponse(taskId.toString());
			return ResponseEntity.ok(taskResponse);
		}
		return null;
	}
	
}
