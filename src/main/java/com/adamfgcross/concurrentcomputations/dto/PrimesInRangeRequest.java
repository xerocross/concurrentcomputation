package com.adamfgcross.concurrentcomputations.dto;

public class PrimesInRangeRequest extends TaskRequest {

	private String rangeMin;
	
	public String getRangeMin() {
		return rangeMin;
	}

	public void setRangeMin(String rangeMin) {
		this.rangeMin = rangeMin;
	}

	public String getMaxRange() {
		return maxRange;
	}

	public void setMaxRange(String maxRange) {
		this.maxRange = maxRange;
	}

	private String maxRange;
}
