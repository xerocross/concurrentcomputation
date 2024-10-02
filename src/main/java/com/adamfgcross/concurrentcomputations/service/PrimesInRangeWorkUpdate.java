package com.adamfgcross.concurrentcomputations.service;

import java.util.Collections;
import java.util.Set;

public class PrimesInRangeWorkUpdate {

	public PrimesInRangeWorkUpdate(Long taskId, Set<String> primes) {
		this.taskId = taskId;
		this.primes = primes;
	}
	
	public static PrimesInRangeWorkUpdate getTerminal() {
		var workUpdate = new PrimesInRangeWorkUpdate(null, Collections.emptySet());
		workUpdate.terminal = true;
		return workUpdate;
	}
	
	private Long taskId;
	private Set<String> primes;
	private boolean terminal = false;
	
	public Set<String> getPrimes() {
		return this.primes;
	}
	
	public Long getTaskId() {
		return taskId;
	}
	
	public boolean isTerminal() {
		return this.terminal;
	}
}
