package com.leorces.engine.job.compaction.handler;

import com.leorces.engine.configuration.properties.CompactionProperties;
import com.leorces.engine.core.CommandDispatcher;
import com.leorces.engine.job.compaction.command.BatchCompactionCommand;
import com.leorces.engine.job.compaction.command.CompactionCommand;
import com.leorces.engine.service.TaskExecutorService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CompactionCommandHandler Tests")
class CompactionCommandHandlerTest {

    @Mock
    private CompactionProperties compactionProperties;

    @Mock
    private TaskExecutorService taskExecutor;

    @Mock
    private CommandDispatcher dispatcher;

    @InjectMocks
    private CompactionCommandHandler handler;

    @Test
    @DisplayName("handle should start multiple compaction jobs based on properties")
    void handleShouldStartMultipleCompactionJobs() {
        // Given
        var maxJobs = 3;
        var batchSize = 100;
        var command = new CompactionCommand();

        when(compactionProperties.maxJobs()).thenReturn(maxJobs);
        when(taskExecutor.submit(any(Runnable.class))).thenReturn(CompletableFuture.completedFuture(null));

        // When
        handler.handle(command);

        // Then
        verify(taskExecutor, times(maxJobs)).submit(any(Runnable.class));
    }

    @Test
    @DisplayName("runCompactionJob should dispatch BatchCompactionCommand")
    void runCompactionJobShouldDispatchBatchCompactionCommand() {
        // Given
        var maxJobs = 1;
        var batchSize = 50;
        var command = new CompactionCommand();

        when(compactionProperties.maxJobs()).thenReturn(maxJobs);
        when(compactionProperties.batchSize()).thenReturn(batchSize);

        // We capture the runnable passed to taskExecutor.submit
        doAnswer(invocation -> {
            Runnable runnable = invocation.getArgument(0);
            runnable.run();
            return CompletableFuture.completedFuture(null);
        }).when(taskExecutor).submit(any(Runnable.class));

        // When
        handler.handle(command);

        // Then
        verify(dispatcher).dispatch(BatchCompactionCommand.of(batchSize));
    }

}
