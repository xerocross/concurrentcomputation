package com.adamfgcross.concurrentcomputations.controller;

import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.adamfgcross.concurrentcomputations.dto.IntFactorTaskRequest;
import com.adamfgcross.concurrentcomputations.dto.IntFactorTaskResponse;
import com.adamfgcross.concurrentcomputations.service.IntFactorService;

@RestController
@RequestMapping("/api/factor")
public class IntFactorTaskController {

	private IntFactorService intFactorService;
	
	public IntFactorTaskController(IntFactorService intFactorService) {
		this.intFactorService = intFactorService;
	}
	
	
	@PostMapping
	public ResponseEntity<IntFactorTaskResponse> submitTask(@RequestBody IntFactorTaskRequest intFactorTaskRequest) {
		Long taskId = intFactorService.submitTask(intFactorTaskRequest);
		IntFactorTaskResponse taskResponse = new IntFactorTaskResponse(taskId);
		return ResponseEntity.ok(taskResponse);
	}
	
	@GetMapping("/{id}")
	public ResponseEntity<IntFactorTaskResponse> getTask(@PathVariable Long id) {
		Optional<IntFactorTaskResponse> taskOptional = intFactorService.getTask(id);
		return taskOptional.map(x -> ResponseEntity.ok(x))
				.orElseGet(() -> ResponseEntity.notFound().build());
	}
	
}
