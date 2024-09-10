package com.adamfgcross.concurrentcomputations.dto;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PrimeRangeValidator implements ConstraintValidator<ValidPrimeRange, PrimesInRangeRequest> {

    @Override
    public boolean isValid(PrimesInRangeRequest request, ConstraintValidatorContext context) {
        return request.getRangeMin() <= request.getRangeMax();
    }
}
