package com.leorces.engine.admin.migration.handler;

import com.leorces.engine.admin.migration.command.ProcessMigrationWithInstructionsCommand;
import com.leorces.engine.admin.migration.command.SingleProcessMigrationCommand;
import com.leorces.engine.configuration.properties.job.ProcessMigrationProperties;
import com.leorces.engine.core.CommandDispatcher;
import com.leorces.engine.service.TaskExecutorService;
import com.leorces.model.definition.ProcessDefinition;
import com.leorces.model.job.Job;
import com.leorces.model.job.migration.ProcessMigrationPlan;
import com.leorces.model.runtime.process.ProcessExecution;
import com.leorces.persistence.AdminPersistence;
import com.leorces.persistence.JobPersistence;
import com.leorces.persistence.ProcessPersistence;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProcessMigrationWithInstructionsCommandHandler Tests")
class ProcessMigrationWithInstructionsCommandHandlerTest {

    @Mock
    private JobPersistence jobPersistence;

    @Mock
    private AdminPersistence adminPersistence;

    @Mock
    private ProcessPersistence processPersistence;

    @Mock
    private TaskExecutorService taskExecutor;

    @Mock
    private CommandDispatcher dispatcher;

    @Mock
    private ProcessMigrationProperties properties;

    private ProcessMigrationWithInstructionsCommandHandler handler;

    @BeforeEach
    void setUp() {
        handler = new ProcessMigrationWithInstructionsCommandHandler(jobPersistence, adminPersistence, processPersistence, taskExecutor, dispatcher, properties);
    }

    @Test
    @DisplayName("should return correct command type")
    void shouldReturnCorrectCommandType() {
        // When & Then
        assertThat(handler.getCommandType()).isEqualTo(ProcessMigrationWithInstructionsCommand.class);
    }

    @Test
    @DisplayName("should migrate processes with instructions in batches")
    void shouldMigrateProcesses() {
        // Given
        var fromDefId = "from-id";
        var toDefId = "to-id";
        var batchSize = 2;
        var maxJobs = 1;

        var fromDef = mock(ProcessDefinition.class);
        when(fromDef.id()).thenReturn(fromDefId);
        var toDef = mock(ProcessDefinition.class);

        var migration = mock(ProcessMigrationPlan.class);
        var job = mock(Job.class);
        var command = ProcessMigrationWithInstructionsCommand.of(fromDef, toDef, migration, null);

        var process1 = mock(ProcessExecution.class);
        var process2 = mock(ProcessExecution.class);

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

        // Loop runs once because batchSize = 2 and findExecutionsForUpdate returns 2, then next call returns 0
        when(processPersistence.findExecutionsForUpdate(fromDefId, batchSize))
                .thenReturn(List.of(process1, process2), List.of());

        // When
        var result = handler.execute(job, command);

        // Then
        assertThat(result).containsEntry("Total migrated processes", 2L);
        verify(dispatcher).dispatch(SingleProcessMigrationCommand.of(process1, toDef, migration));
        verify(dispatcher).dispatch(SingleProcessMigrationCommand.of(process2, toDef, migration));
    }

}
