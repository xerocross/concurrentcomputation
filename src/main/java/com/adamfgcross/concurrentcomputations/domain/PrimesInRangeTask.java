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
	private Long rangeMin;
	private Long rangeMax;
	
	public PrimesInRangeTask() {}
	
	public PrimesInRangeTask(Long rangeMin, Long rangeMax) {
		this.rangeMin = rangeMin;
		this.rangeMax = rangeMax;
	}
	
	public Long getRangeMax() {
		return rangeMax;
	}

	public void setRangeMax(Long rangeMax) {
		this.rangeMax = rangeMax;
	}

	@ElementCollection
    @CollectionTable(name = "prime_numbers", joinColumns = @JoinColumn(name = "prime_number_id"))
    @Column(name = "primes")
	private Set<String> primes = new HashSet<>();
	
	public Set<String> getPrimes() {
		return primes;
	}

	public void setPrimes(Set<String> primes) {
		this.primes = primes;
	}

	public Long getRangeMin() {
		return rangeMin;
	}

	public void setRangeMin(Long rangeMin) {
		this.rangeMin = rangeMin;
	}
}
