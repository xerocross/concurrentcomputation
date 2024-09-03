package com.adamfgcross.concurrentcomputations.dto;

public class TaskResponse {
	private String taskId;

	public TaskResponse(String taskId) {
		this.taskId = taskId;
	}
	
	public String getTaskId() {
		return taskId;
	}

	public void setTaskId(String taskId) {
		this.taskId = taskId;
	}
}
