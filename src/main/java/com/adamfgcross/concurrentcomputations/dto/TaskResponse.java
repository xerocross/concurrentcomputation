package com.adamfgcross.concurrentcomputations.dto;

public class TaskResponse {
	private Long id;

	private Boolean isCompleted = false;
	
	public Boolean getIsCompleted() {
		return isCompleted;
	}

	public void setIsCompleted(Boolean isCompleted) {
		this.isCompleted = isCompleted;
	}

	public TaskResponse() {
	}
	
	public TaskResponse(Long id) {
		this.id = id;
	}
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}
}
