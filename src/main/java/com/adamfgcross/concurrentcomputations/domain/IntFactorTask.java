package com.adamfgcross.concurrentcomputations.domain;

import jakarta.persistence.Entity;

@Entity
public class IntFactorTask extends Task {

	private String integerForFactoring;

	public String getIntegerForFactoring() {
		return integerForFactoring;
	}

	public void setIntegerForFactoring(String integerForFactoring) {
		this.integerForFactoring = integerForFactoring;
	}
}
