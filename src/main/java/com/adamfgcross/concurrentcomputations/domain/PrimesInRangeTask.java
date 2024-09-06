package com.adamfgcross.concurrentcomputations.domain;

import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;

@Entity
public class PrimesInRangeTask extends Task {
	private String rangeMin;
	private String rangeMax;
	
	public String getRangeMax() {
		return rangeMax;
	}

	public void setRangeMax(String rangeMax) {
		this.rangeMax = rangeMax;
	}

	@ElementCollection
    @CollectionTable(name = "prime_numbers", joinColumns = @JoinColumn(name = "prime_number_id"))
    @Column(name = "primes")
	private Set<String> primes = new HashSet<>();
	
	public String getRangeMin() {
		return rangeMin;
	}

	public void setRangeMin(String rangeMin) {
		this.rangeMin = rangeMin;
	}
}
