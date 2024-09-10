package com.adamfgcross.concurrentcomputations.dto;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = PrimeRangeValidator.class)
public @interface ValidPrimeRange {
	String message() default "Invalid prime range: rangeMin must be less than or equal to rangeMax";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
