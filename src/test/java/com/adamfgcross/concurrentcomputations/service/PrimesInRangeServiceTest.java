package com.adamfgcross.concurrentcomputations.service;

import static org.mockito.Mockito.*;
import static org.mockito.Mockito.when;

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
        // Initialize the mocks
        MockitoAnnotations.openMocks(this);
        
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
	
}
