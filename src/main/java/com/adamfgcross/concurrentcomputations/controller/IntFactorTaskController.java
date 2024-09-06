package com.adamfgcross.concurrentcomputations.controller;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.adamfgcross.concurrentcomputations.JwtUtil;
import com.adamfgcross.concurrentcomputations.domain.CustomUserDetails;
import com.adamfgcross.concurrentcomputations.domain.User;
import com.adamfgcross.concurrentcomputations.dto.IntFactorTaskRequest;
import com.adamfgcross.concurrentcomputations.dto.IntFactorTaskResponse;
import com.adamfgcross.concurrentcomputations.service.IntFactorService;
import com.adamfgcross.concurrentcomputations.service.UserService;

@RestController
@RequestMapping("/api/factor")
public class IntFactorTaskController {

	private IntFactorService intFactorService;
	private UserService userService;
	private UserDetailsService userDetailsService;
	private JwtUtil jwtUtil;
	
	public IntFactorTaskController(IntFactorService intFactorService, 
			UserService userService,
			UserDetailsService userDetailsService,
			JwtUtil jwtUtil) {
		this.intFactorService = intFactorService;
		this.userService = userService;
		this.userDetailsService = userDetailsService;
		this.jwtUtil = jwtUtil;
	}
	
	private static final Logger logger = LoggerFactory.getLogger(IntFactorTaskController.class);
	
	
	@PostMapping
	public ResponseEntity<IntFactorTaskResponse> submitTask(@RequestBody IntFactorTaskRequest intFactorTaskRequest,
			Authentication authentication) {
		User user = null;
		boolean isAnonymous = false;
		logger.info("received factor request");
		if (authentication != null && authentication.isAuthenticated()) {
			logger.info("authenticated");
            String username = authentication.getName();
            CustomUserDetails userDetails = (CustomUserDetails) userDetailsService.loadUserByUsername(username);
            user = userDetails.getUser();
        } else if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
        	isAnonymous = true;
        	logger.info("anonymous user");
			user = userService.createAnonymousUser();
		}
		Long taskId = intFactorService.submitTask(user, intFactorTaskRequest);
		IntFactorTaskResponse taskResponse = new IntFactorTaskResponse(taskId);
		if (isAnonymous) {
			String token = jwtUtil.generateAnonymousUserToken(user);
			logger.info("generated token: " + token);
			taskResponse.setToken(token);
		}
		return ResponseEntity.ok(taskResponse);
	}
	
	@GetMapping("/{id}")
	public ResponseEntity<IntFactorTaskResponse> getTask(@PathVariable Long id) {
		Optional<IntFactorTaskResponse> taskOptional = intFactorService.getTask(id);
		return taskOptional.map(x -> ResponseEntity.ok(x))
				.orElseGet(() -> ResponseEntity.notFound().build());
	}
	
}
