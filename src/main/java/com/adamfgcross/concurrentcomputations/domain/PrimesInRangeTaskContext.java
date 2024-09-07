package com.adamfgcross.concurrentcomputations.domain;

import java.util.HashSet;
import java.util.Set;

public class PrimesInRangeTaskContext {
	private Long taskId;
	private String rangeMin;
	private PrimesInRangeTask primesInRangeTask;
	public String getRangeMin() {
		return rangeMin;
	}

	public PrimesInRangeTask getPrimesInRangeTask() {
		return primesInRangeTask;
	}

	public void setPrimesInRangeTask(PrimesInRangeTask primesInRangeTask) {
		this.primesInRangeTask = primesInRangeTask;
	}

	public void setRangeMin(String rangeMin) {
		this.rangeMin = rangeMin;
	}

	public String getRangeMax() {
		return rangeMax;
	}

	public void setRangeMax(String rangeMax) {
		this.rangeMax = rangeMax;
	}

	private String rangeMax;

	public Long getTaskId() {
		return taskId;
	}

	public void setTaskId(Long taskId) {
		this.taskId = taskId;
	}
	
	private Set<String> primes = new HashSet<>();

	public Set<String> getPrimes() {
		return primes;
	}

	public void setPrimes(Set<String> primes) {
		this.primes = primes;
	}
}
