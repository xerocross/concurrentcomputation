package com.adamfgcross.concurrentcomputations.security;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import io.github.bucket4j.Bucket;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class RateLimitFilter implements Filter {

	private final Map<String, Bucket> ipTokenBuckets = new ConcurrentHashMap<>();
	
	@Value("${spring.rate-limit.anonymous.capacity}")
	private int CAPACITY;
	
	@Value("${spring.rate-limit.anonymous.refill-rate}")
	private int REFILL_RATE;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // Initialization if needed
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String ip = httpRequest.getRemoteAddr();  // Get client IP address
        Bucket bucket = resolveBucket(ip);

        if (bucket.tryConsume(1)) {
            // Proceed with the request if the token is available
            chain.doFilter(request, response);
        } else {
            // If the request is rate-limited, return 429 (Too Many Requests)
            httpResponse.sendError(HttpStatus.TOO_MANY_REQUESTS.value(), "too many requests; try again later.");
        }
    }

    @Override
    public void destroy() {
        // Cleanup if needed
    }

    private Bucket resolveBucket(String ip) {
        return ipTokenBuckets.computeIfAbsent(ip, this::newBucket);
    }

    private Bucket newBucket(String ip) {
    	return Bucket.builder()
    	.addLimit(limit -> limit.capacity(CAPACITY).refillGreedy(REFILL_RATE, Duration.ofMinutes(1)))
    	.build();
    }
}
