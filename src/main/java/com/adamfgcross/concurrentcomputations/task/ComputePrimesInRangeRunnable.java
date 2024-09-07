package com.adamfgcross.concurrentcomputations.task;

import java.util.ArrayList;
import java.util.List;

public class ComputePrimesInRangeRunnable implements Runnable {

	private Long rangeMin;
	private Long rangeMax;
	private List<Long> primes = new ArrayList<>();
	
	public ComputePrimesInRangeRunnable(Long rangeMin, Long rangeMax) {
		this.rangeMin = rangeMin;
		this.rangeMax = rangeMax;
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		for (long i = rangeMin; i < rangeMax; i++) {
			if (isPrime(i)) {
				primes.add(i);
			}
		}
	}

	private boolean isPrime(Long number) {
		if (number < 2) {
            return false;
        }

        // Check divisibility up to the square root of the number
        for (long i = 2; i * i <= number; i++) {
            if (number % i == 0) {
                return false;  // Not prime if divisible by i
            }
        }

        return true;  // Prime if no divisors found
	}
	
}
