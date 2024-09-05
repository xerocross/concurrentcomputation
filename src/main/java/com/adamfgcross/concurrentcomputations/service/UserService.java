package com.adamfgcross.concurrentcomputations.service;

import org.springframework.stereotype.Service;

import com.adamfgcross.concurrentcomputations.domain.User;
import com.adamfgcross.concurrentcomputations.repository.UserRepository;

@Service
public class UserService {

	private final TokenGenerator tokenGenerator;
	private final UserRepository userRepository;
	
	public UserService (TokenGenerator tokenGenerator,
			UserRepository userRepository) {
		this.tokenGenerator = tokenGenerator;
		this.userRepository = userRepository;
	}
	
	public User createAnonymousUser() {
		User user = new User();
		String tempKey = tokenGenerator.generateToken();
		
		while (userRepository.existsByTempKey(tempKey)) {
			tempKey = tokenGenerator.generateToken();
		}
		user.setTempKey(tempKey);
		userRepository.save(user);
		return user;
	}
}
