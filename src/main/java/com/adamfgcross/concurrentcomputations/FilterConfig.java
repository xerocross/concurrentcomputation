package com.adamfgcross.concurrentcomputations;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.adamfgcross.concurrentcomputations.security.RateLimitFilter;

@Configuration
public class FilterConfig {

	@Bean
	public FilterRegistrationBean<RateLimitFilter> rateLimitFilter() {
		FilterRegistrationBean<RateLimitFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new RateLimitFilter());
        registrationBean.addUrlPatterns("/api/primes/*");  // Apply filter to prime API endpoints
        registrationBean.addUrlPatterns("/api/factor/*");
        registrationBean.setOrder(1);  // Set the order if you have multiple filters
        return registrationBean;
	}
}
