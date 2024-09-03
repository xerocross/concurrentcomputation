package com.adamfgcross.concurrentcomputations.dto;

public class IntFactorTaskRequest extends TaskRequest {

	private String intToFactor;

	public String getIntToFactor() {
		return intToFactor;
	}

	public void setIntToFactor(String intToFactor) {
		this.intToFactor = intToFactor;
	}
}
