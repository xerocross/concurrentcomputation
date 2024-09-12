package com.adamfgcross.concurrentcomputations.domain;

import java.util.HashSet;
import java.util.Set;

public class PrimesInRangeTaskContext extends TaskContext {
	private Long taskId;
	private Long rangeMin;
	private PrimesInRangeTask primesInRangeTask;
	
	public Long getRangeMin() {
		return rangeMin;
	}

	public PrimesInRangeTask getPrimesInRangeTask() {
		return primesInRangeTask;
	}

	public void setPrimesInRangeTask(PrimesInRangeTask primesInRangeTask) {
		this.primesInRangeTask = primesInRangeTask;
	}

	public void setRangeMin(Long rangeMin) {
		this.rangeMin = rangeMin;
	}

	public Long getRangeMax() {
		return rangeMax;
	}

	public void setRangeMax(Long rangeMax) {
		this.rangeMax = rangeMax;
	}

	private Long rangeMax;

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
