package com.adamfgcross.concurrentcomputations.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.adamfgcross.concurrentcomputations.domain.User;

public interface UserRepository extends JpaRepository<User, Long> {

	boolean existsByTempKey(String tempKey);
	
	Optional<User> findByUsername(String username);
}
