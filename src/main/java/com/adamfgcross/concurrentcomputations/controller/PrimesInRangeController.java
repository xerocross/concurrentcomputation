package com.adamfgcross.concurrentcomputations.controller;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.adamfgcross.concurrentcomputations.domain.PrimesInRangeTask;
import com.adamfgcross.concurrentcomputations.dto.PrimesInRangeRequest;
import com.adamfgcross.concurrentcomputations.dto.PrimesInRangeResponse;
import com.adamfgcross.concurrentcomputations.service.PrimesInRangeService;

@RestController
@RequestMapping("/api/primes")
public class PrimesInRangeController {

	private PrimesInRangeService primesInRangeService;
	
	public PrimesInRangeController(PrimesInRangeService primesInRangeService) {
		this.primesInRangeService = primesInRangeService;
	}
	
	@PostMapping
	public ResponseEntity<PrimesInRangeResponse> startComputingPrimesInRange(@RequestBody PrimesInRangeRequest primesInRangeRequest) {
		PrimesInRangeTask primesInRangeTask =  primesInRangeService.initiatePrimesInRangeTask(null, primesInRangeRequest);
		var primesInRangeResponse = new PrimesInRangeResponse();
		primesInRangeResponse.setId(primesInRangeTask.getId());
		return ResponseEntity.ok(primesInRangeResponse);
	}
	
	@GetMapping("/{id}")
	public ResponseEntity<PrimesInRangeResponse> getPrimesInRangeResult(@PathVariable Long id) {
		return primesInRangeService.getTask(id)
				.map(ResponseEntity::ok)
				.orElseGet(() -> ResponseEntity.notFound().build());
	}
	
}
