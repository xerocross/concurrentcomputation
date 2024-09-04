package com.adamfgcross.concurrentcomputations.domain;

import java.util.List;

public class IntFactorTaskContext {
	
	public IntFactorTaskContext() {
	}
	
	public IntFactorTaskContext(IntFactorTask intFactorTask) {
		this.intFactorTask = intFactorTask;
		String numberInput = intFactorTask.getIntegerForFactoring();
		this.number = Long.parseLong(numberInput);
		
	}
	
	private Long number;
	
	private List<Long> factors;
	
	private User user;
	
	private Long taskId;
	
	private IntFactorTask intFactorTask;

	public IntFactorTask getIntFactorTask() {
		return intFactorTask;
	}

	public void setIntFactorTask(IntFactorTask intFactorTask) {
		this.intFactorTask = intFactorTask;
	}

	public Long getNumber() {
		return number;
	}

	public void setNumber(Long number) {
		this.number = number;
	}

	public List<Long> getFactors() {
		return factors;
	}

	public void setFactors(List<Long> factors) {
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
