package com.adamfgcross.concurrentcomputations.domain;

import java.util.List;

public class IntFactorTaskContext {
	
	public IntFactorTaskContext() {
	}
	
	public IntFactorTaskContext(IntFactorTask intFactorTask) {
		this.intFactorTask = intFactorTask;
		String numberInput = intFactorTask.getNumber();
		this.number = numberInput;
		
	}
	
	private String number;
	
	private List<String> factors;
	
	private User user;
	
	private Long taskId;
	
	private IntFactorTask intFactorTask;

	public IntFactorTask getIntFactorTask() {
		return intFactorTask;
	}

	public void setIntFactorTask(IntFactorTask intFactorTask) {
		this.intFactorTask = intFactorTask;
	}

	public String getNumber() {
		return number;
	}

	public void setNumber(String number) {
		this.number = number;
	}

	public List<String> getFactors() {
		return factors;
	}

	public void setFactors(List<String> factors) {
		this.factors = factors;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public Long getTaskId() {
		return taskId;
	}

	public void setTaskId(Long taskId) {
		this.taskId = taskId;
	}

}
