package com.leorces.engine.admin.compaction.handler;

import com.leorces.engine.admin.common.model.JobType;
import com.leorces.engine.admin.compaction.command.CompactionCommand;
import com.leorces.engine.configuration.properties.job.CompactionProperties;
import com.leorces.engine.service.TaskExecutorService;
import com.leorces.model.job.Job;
import com.leorces.model.runtime.process.ProcessExecution;
import com.leorces.persistence.AdminPersistence;
import com.leorces.persistence.HistoryPersistence;
import com.leorces.persistence.JobPersistence;
import com.leorces.persistence.ProcessPersistence;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CompactionCommandHandler Tests")
class CompactionCommandHandlerTest {

    private static final String OUTPUT_KEY = "Total compacted processes";

    @Mock
    private JobPersistence jobPersistence;

    @Mock
    private ProcessPersistence processPersistence;

    @Mock
    private HistoryPersistence historyPersistence;

    @Mock
    private AdminPersistence adminPersistence;

    @Mock
    private CompactionProperties properties;

    @Mock
    private TaskExecutorService taskExecutor;

    @InjectMocks
    private CompactionCommandHandler handler;

    @Test
    @DisplayName("should return correct command type")
    void shouldReturnCorrectCommandType() {
        // When & Then
        assertThat(handler.getCommandType()).isEqualTo(CompactionCommand.class);
    }

    @Test
    @DisplayName("should compact processes in batches")
    void shouldCompactProcesses() throws Exception {
        // Given
        var batchSize = 10;
        var maxJobs = 2;
        var job = mock(Job.class);
        var command = CompactionCommand.manual();

        when(properties.batchSize()).thenReturn(batchSize);
        when(properties.maxJobs()).thenReturn(maxJobs);

        // Mock task executor to run immediately
        when(taskExecutor.supplyAsync(any())).thenAnswer(invocation -> {
            Callable<Long> callable = invocation.getArgument(0);
            return CompletableFuture.completedFuture(callable.call());
        });

        // Mock admin persistence to run immediately
        when(adminPersistence.execute(any())).thenAnswer(invocation -> {
            Supplier<Integer> supplier = invocation.getArgument(0);
            return supplier.get();
        });

        var process1 = mock(ProcessExecution.class);
        var process2 = mock(ProcessExecution.class);

        // Job 1, Batch 1: returns 10
        // Job 1, Batch 2: returns 5 (stops loop for job 1)
        // Job 2, Batch 1: returns 3 (stops loop for job 2)

        when(processPersistence.findAllFullyCompletedForUpdate(batchSize))
                .thenReturn(
                        List.of(process1, process1, process1, process1, process1, process1, process1, process1, process1, process1),
                        List.of(process1, process1, process1, process1, process1),
                        List.of(process2, process2, process2)
                );

        // When
        var result = handler.execute(job, command);

        // Then
        assertThat(result).containsEntry(OUTPUT_KEY, 18L);
        verify(processPersistence, times(3)).findAllFullyCompletedForUpdate(batchSize);
        verify(historyPersistence, times(3)).save(anyList());
    }

    @Test
    @DisplayName("should handle empty results gracefully")
    void shouldHandleEmptyResults() throws Exception {
        // Given
        var batchSize = 10;
        var maxJobs = 1;
        var job = mock(Job.class);
        var command = CompactionCommand.cron();

        when(properties.batchSize()).thenReturn(batchSize);
        when(properties.maxJobs()).thenReturn(maxJobs);

        when(taskExecutor.supplyAsync(any())).thenAnswer(invocation -> {
            Callable<Long> callable = invocation.getArgument(0);
            return CompletableFuture.completedFuture(callable.call());
        });

        when(adminPersistence.execute(any())).thenAnswer(invocation -> {
            Supplier<Integer> supplier = invocation.getArgument(0);
            return supplier.get();
        });

        when(processPersistence.findAllFullyCompletedForUpdate(batchSize)).thenReturn(List.of());

        // When
        var result = handler.execute(job, command);

        // Then
        assertThat(result).containsEntry(OUTPUT_KEY, 0L);
        verify(processPersistence).findAllFullyCompletedForUpdate(batchSize);
        verify(historyPersistence, never()).save(anyList());
    }

    @Test
    @DisplayName("should handle process through handle method and verify job persistence")
    void shouldHandleJob() {
        // Given
        var batchSize = 10;
        var maxJobs = 1;
        var command = CompactionCommand.manual();
        var job = mock(Job.class);
        when(job.toBuilder()).thenReturn(Job.builder());

        when(jobPersistence.run(any())).thenReturn(job);

        when(properties.batchSize()).thenReturn(batchSize);
        when(properties.maxJobs()).thenReturn(maxJobs);

        when(taskExecutor.supplyAsync(any())).thenAnswer(invocation -> {
            Callable<Long> callable = invocation.getArgument(0);
            return CompletableFuture.completedFuture(callable.call());
        });

        when(adminPersistence.execute(any())).thenAnswer(invocation -> {
            Supplier<Integer> supplier = invocation.getArgument(0);
            return supplier.get();
        });

        when(processPersistence.findAllFullyCompletedForUpdate(batchSize)).thenReturn(List.of());

        // When
        handler.handle(command);

        // Then
        verify(jobPersistence).run(argThat(j -> j.type().equals(JobType.COMPACTION.toString())));
        verify(jobPersistence).complete(any());
    }

    @Test
    @DisplayName("should handle exception and fail job")
    void shouldHandleException() {
        // Given
        var command = CompactionCommand.manual();
        var job = mock(Job.class);
        when(job.toBuilder()).thenReturn(Job.builder());

        when(jobPersistence.run(any())).thenReturn(job);

        when(properties.maxJobs()).thenThrow(new RuntimeException("Test error"));

        // When
        handler.handle(command);

        // Then
        verify(jobPersistence).fail(argThat(j -> "Test error".equals(j.failureReason())));
    }

}
