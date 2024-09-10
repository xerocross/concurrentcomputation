package com.adamfgcross.concurrentcomputations.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@ValidPrimeRange
public class PrimesInRangeRequest extends TaskRequest {

	@NotNull(message = "the rangeMin must not be null")
	@Min(value = 0)
	@Max(value = Long.MAX_VALUE)
	private Long rangeMin;
	
	@NotNull(message = "the rangeMax must not be null")
	@Min(value = 0)
	@Max(value = Long.MAX_VALUE)
	private Long rangeMax;
	
	public Long getRangeMax() {
		return rangeMax;
	}

	public void setRangeMax(Long rangeMax) {
		this.rangeMax = rangeMax;
	}

	public Long getRangeMin() {
		return rangeMin;
	}

	public void setRangeMin(Long rangeMin) {
		this.rangeMin = rangeMin;
	}

}
