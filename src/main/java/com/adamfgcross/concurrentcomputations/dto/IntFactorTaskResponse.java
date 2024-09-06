package com.adamfgcross.concurrentcomputations.dto;

import java.util.List;

public class IntFactorTaskResponse extends TaskResponse {
	private String number;
	private List<String> factors;
	
	private String token;
	
	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public IntFactorTaskResponse() {
		super();
	}
	
	public IntFactorTaskResponse(Long id) {
		super(id);
	}

	public String getNumber() {
		return number;
	}

	public void setNumber(String number) {
		this.number = number;
	}

	public List<String> getFactors() {
		return factors;
	}

	public void setFactors(List<String> factors) {
		this.factors = factors;
	}
}
