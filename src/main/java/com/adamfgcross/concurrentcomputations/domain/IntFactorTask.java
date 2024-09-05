package com.adamfgcross.concurrentcomputations.domain;

import java.util.List;

import jakarta.persistence.Entity;

@Entity
public class IntFactorTask extends Task {

	private String number;
	
	private List<String> factors;

	public List<String> getFactors() {
		return factors;
	}

	public void setFactors(List<String> factors) {
		this.factors = factors;
	}

	public String getNumber() {
		return number;
	}

	public void setNumber(String number) {
		this.number = number;
	}
}
