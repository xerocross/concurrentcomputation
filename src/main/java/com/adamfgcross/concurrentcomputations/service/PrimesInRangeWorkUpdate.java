package com.adamfgcross.concurrentcomputations.service;

import java.util.Collections;
import java.util.Set;

public class PrimesInRangeWorkUpdate {

	public PrimesInRangeWorkUpdate(Set<String> primes) {
		this.primes = primes;
	}
	
	public static PrimesInRangeWorkUpdate getTerminal() {
		var workUpdate = new PrimesInRangeWorkUpdate(Collections.emptySet());
		workUpdate.terminal = true;
		return workUpdate;
	}
	
	private Set<String> primes;
	private boolean terminal = false;
	
	public Set<String> getPrimes() {
		return this.primes;
	}
	
	public boolean isTerminal() {
		return this.terminal;
	}
}
