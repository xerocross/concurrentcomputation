package com.adamfgcross.concurrentcomputations.service;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Optional;
import java.util.concurrent.Executor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.adamfgcross.concurrentcomputations.domain.PrimesInRangeTaskContext;
import com.adamfgcross.concurrentcomputations.domain.User;
import com.adamfgcross.concurrentcomputations.domain.PrimesInRangeTask;
import com.adamfgcross.concurrentcomputations.dto.PrimesInRangeRequest;
import com.adamfgcross.concurrentcomputations.helper.PrimesInRangeHelper;
import com.adamfgcross.concurrentcomputations.repository.TaskRepository;

public class PrimesInRangeServiceTest {

	
	@Mock
	private PrimesInRangeHelper primesInRangeHelper;
	
	@Mock
	private TaskRepository taskRepository;
	
	@Mock
    private Executor executor;
	
	@InjectMocks
	private PrimesInRangeService primesInRangeService;
	
	
	@BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        
        // inject a synchronous executor
        Mockito.doAnswer((invocation) -> {
        	Runnable runnable = invocation.getArgument(0);
        	runnable.run();
            return null;
        }).when(executor).execute(any(Runnable.class));
    }
	
	@Test
	public void initiatePrimesInRangeTask_callsComputePrimesInRangeOnHelper() {
		PrimesInRangeRequest primesInRangeRequest = new PrimesInRangeRequest();
		primesInRangeRequest.setRangeMin(1L);
		primesInRangeRequest.setRangeMax(10L);
		User user = new User();
		primesInRangeService.initiatePrimesInRangeTask(user, primesInRangeRequest);
		verify(primesInRangeHelper).computePrimesInRange(any());
	}
	
	@Test
	public void initiatePrimesInRangeTask_createsAndSavesTask() {
		PrimesInRangeRequest primesInRangeRequest = new PrimesInRangeRequest();
		primesInRangeRequest.setRangeMin(1L);
		primesInRangeRequest.setRangeMax(10L);
		User user = new User();
		primesInRangeService.initiatePrimesInRangeTask(user, primesInRangeRequest);
		ArgumentCaptor<PrimesInRangeTask> captor = ArgumentCaptor.forClass(PrimesInRangeTask.class);
		verify(taskRepository).save(captor.capture());
		var primesInRangeTask = captor.getValue();
		assertEquals(primesInRangeTask.getRangeMax(), 10L);
		assertEquals(primesInRangeTask.getRangeMin(), 1L);
		
	}
	
	@Test
	public void getTask_ifPrimesInRangeTaskExists_returnsPrimesInRangeResponseDTOInOptional() {
		Long testTaskId = 10L;
		var task = getTestPrimesInRangeTask();
		task.setRangeMax(10L);
		task.setRangeMin(1L);
		when(taskRepository.findById(testTaskId)).thenReturn(Optional.of(task));
		var result = primesInRangeService.getTask(testTaskId);
		var taskDTO = result.get();
		assertEquals("10", taskDTO.getRangeMax());
		assertEquals("1", taskDTO.getRangeMin());
	}
	
	@Test
	public void getTask_ifTaskCompleted_responseIndicatesCompleted() {
		Long testTaskId = 10L;
		var task = getTestPrimesInRangeTask();
		task.setIsCompleted(true);
		when(taskRepository.findById(testTaskId)).thenReturn(Optional.of(task));
		var result = primesInRangeService.getTask(testTaskId);
		var taskDTO = result.get();
		assertEquals(true, taskDTO.getIsCompleted());
	}
	
	@Test
	public void getTask_ifPrimesInRangeTaskDoesNotExist_returnsEmptyOptional() {
		Long testTaskId = 10L;
		when(taskRepository.findById(testTaskId)).thenReturn(Optional.empty());
		var result = primesInRangeService.getTask(testTaskId);
		assertTrue(result.isEmpty());
	}
	
	private PrimesInRangeTask getTestPrimesInRangeTask() {
		var task = new PrimesInRangeTask();
		task.setId(10L);
		task.setRangeMin(1L);
		task.setRangeMax(10L);
		return task;
	}
}
