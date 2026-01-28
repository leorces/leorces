package com.leorces.engine.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.task.AsyncTaskExecutor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TaskExecutorService Tests")
class TaskExecutorServiceTest {

    @Mock
    private AsyncTaskExecutor taskExecutor;

    @InjectMocks
    private TaskExecutorService service;

    @Test
    @DisplayName("execute should delegate task to taskExecutor")
    void executeShouldDelegateTask() {
        // Given
        var task = mock(Runnable.class);

        // When
        service.execute(task);

        // Then
        verify(taskExecutor).execute(task);
        verifyNoMoreInteractions(taskExecutor);
    }

    @Test
    @DisplayName("submit should run task asynchronously")
    void runAsyncShouldRunTaskAsync() throws Exception {
        // Given
        var task = mock(Runnable.class);

        doAnswer(invocation -> {
            Runnable r = invocation.getArgument(0);
            r.run();
            return null;
        }).when(taskExecutor).execute(any(Runnable.class));

        // When
        var future = service.runAsync(task);

        // Then
        future.join();
        verify(taskExecutor).execute(any(Runnable.class));
        verify(task, atLeastOnce()).run();
        assertThat(future).isCompleted();
    }

}
