package com.adamfgcross.concurrentcomputations.helper;

import static org.mockito.ArgumentMatchers.any;

import java.util.concurrent.ExecutorService;

import org.junit.jupiter.api.BeforeEach;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.adamfgcross.concurrentcomputations.domain.PrimesInRangeTask;
import com.adamfgcross.concurrentcomputations.domain.PrimesInRangeTaskContext;
import com.adamfgcross.concurrentcomputations.repository.TaskRepository;
import com.adamfgcross.concurrentcomputations.service.TaskStoreService;

public class PrimesInRangeHelperTest {

	@Mock
	private TaskRepository taskRepository;
	
	@Mock
	private TaskStoreService taskStoreService;
	
	@Mock
	private TaskExecutorFactory taskExecutorFactory;
	
	@InjectMocks
	private PrimesInRangeHelper primesInRangeHelper;
	
	@BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        
        // inject a synchronous executor
//        Mockito.doAnswer((invocation) -> {
//        	Runnable runnable = invocation.getArgument(0);
//        	runnable.run();
//            return null;
//        }).when(executor).execute(any(Runnable.class));
    }
	
	public void computePrimesInRange_schedulesAsyncSubtasks() {
		var task = new PrimesInRangeTask(1L, 2500L);
		task.setId(1L);
		var taskContext = getTestPrimesInRangeTaskContext(task);
		primesInRangeHelper.computePrimesInRange(taskContext);
	}
	
	
	private PrimesInRangeTaskContext getTestPrimesInRangeTaskContext(PrimesInRangeTask primesInRangeTask) {
		var primesInRangeTaskContext = new PrimesInRangeTaskContext();
		primesInRangeTaskContext.setPrimesInRangeTask(primesInRangeTask);
		return primesInRangeTaskContext;
	}
}


