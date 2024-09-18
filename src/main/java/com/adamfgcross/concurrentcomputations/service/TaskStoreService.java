package com.adamfgcross.concurrentcomputations.service;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;


public class TaskStoreService<T> {

	private ConcurrentHashMap<Long, Set<CompletableFuture<T>>> taskFutureMap;
	
	public TaskStoreService() {
		taskFutureMap = new ConcurrentHashMap<>();
	}
	
	public void storeTaskFuture(Long taskId, CompletableFuture<T> future) {
		taskFutureMap.computeIfAbsent(taskId, k -> ConcurrentHashMap.newKeySet()).add(future);
	}
	
	public void storeTaskFutures(Long taskId, Collection<CompletableFuture<T>> futures) {
		taskFutureMap.computeIfAbsent(taskId, k -> ConcurrentHashMap.newKeySet()).addAll(futures);
	}
	
	public Optional<Set<CompletableFuture<T>>> getTaskFutures(Long taskId) {
		return Optional.ofNullable(taskFutureMap.get(taskId));
	}
	
	public void removeFuture(Long taskId, CompletableFuture<?> future) {
		taskFutureMap.computeIfPresent(taskId,(key, futures) -> {
			futures.remove(future);
			return futures;
		});
	}
	
	public void removeTaskFutures(Long taskId) {
		taskFutureMap.remove(taskId);
	}
}
