package com.adamfgcross.concurrentcomputations.dto;

public class PrimesInRangeRequest extends TaskRequest {

	private String rangeMin;
	private String rangeMax;
	
	public String getRangeMax() {
		return rangeMax;
	}

	public void setRangeMax(String rangeMax) {
		this.rangeMax = rangeMax;
	}

	public String getRangeMin() {
		return rangeMin;
	}

	public void setRangeMin(String rangeMin) {
		this.rangeMin = rangeMin;
	}

}
