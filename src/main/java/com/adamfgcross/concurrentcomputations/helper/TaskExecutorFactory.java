package com.adamfgcross.concurrentcomputations.helper;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class TaskExecutorFactory {

	@Value("${spring.concurrency.default.num-threads}")
	private int numThreads;
	
	public ExecutorService getTaskExecutor() {
		ExecutorService executorService = Executors.newFixedThreadPool(numThreads);
		return executorService;
	}
}
