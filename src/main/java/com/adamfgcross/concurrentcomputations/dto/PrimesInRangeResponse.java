package com.adamfgcross.concurrentcomputations.dto;

import java.util.ArrayList;
import java.util.List;

import com.adamfgcross.concurrentcomputations.domain.PrimesInRangeTask;

public class PrimesInRangeResponse extends TaskResponse {
	
	public PrimesInRangeResponse() {}
	
	public PrimesInRangeResponse(PrimesInRangeTask primesInRangeTask) {
		this.setId(primesInRangeTask.getId());
		this.setPrimes(primesInRangeTask.getPrimes().stream().toList());
		this.setIsCompleted(primesInRangeTask.getIsCompleted());
	}

	public String getRangeMin() {
		return rangeMin;
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

	private String rangeMin;
	private String rangeMax;
	private List<String> primes = new ArrayList<>();

	public List<String> getPrimes() {
		return primes;
	}

	public void setPrimes(List<String> primes) {
		this.primes = primes;
	}
}
