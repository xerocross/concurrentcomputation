package com.adamfgcross.concurrentcomputations.service;

import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;

import org.springframework.stereotype.Component;


@Component
public class TaskStoreService {

	private ConcurrentHashMap<Long, Set<Future<?>>> taskFutureMap;
	
	public TaskStoreService() {
		taskFutureMap = new ConcurrentHashMap<>();
	}
	
	public void storeTaskFuture(Long taskId, Future<?> future) {
		taskFutureMap.computeIfAbsent(taskId, k -> ConcurrentHashMap.newKeySet()).add(future);
	}
	
	public Optional<Set<Future<?>>> getTaskFutures(Long taskId) {
		return Optional.ofNullable(taskFutureMap.get(taskId));
	}
	
	public void removeFuture(Long taskId, Future<?> future) {
		taskFutureMap.computeIfPresent(taskId,(key, futures) -> {
			futures.remove(future);
			return futures;
		});
	}
	
	public void removeTaskFutures(Long taskId) {
		taskFutureMap.remove(taskId);
	}
}
