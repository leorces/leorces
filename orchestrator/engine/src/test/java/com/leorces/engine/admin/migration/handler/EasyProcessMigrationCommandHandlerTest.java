package com.leorces.engine.admin.migration.handler;

import com.leorces.engine.admin.migration.command.EasyProcessMigrationCommand;
import com.leorces.engine.configuration.properties.job.ProcessMigrationProperties;
import com.leorces.engine.service.TaskExecutorService;
import com.leorces.model.definition.ProcessDefinition;
import com.leorces.model.job.Job;
import com.leorces.persistence.AdminPersistence;
import com.leorces.persistence.ProcessPersistence;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("EasyProcessMigrationCommandHandler Tests")
class EasyProcessMigrationCommandHandlerTest {

    @Mock
    private AdminPersistence adminPersistence;

    @Mock
    private ProcessPersistence processPersistence;

    @Mock
    private TaskExecutorService taskExecutor;

    @Mock
    private ProcessMigrationProperties properties;

    @InjectMocks
    private EasyProcessMigrationCommandHandler handler;

    @Test
    @DisplayName("should return correct command type")
    void shouldReturnCorrectCommandType() {
        // When & Then
        assertThat(handler.getCommandType()).isEqualTo(EasyProcessMigrationCommand.class);
    }

    @Test
    @DisplayName("should migrate processes in batches")
    void shouldMigrateProcesses() {
        // Given
        var fromDefId = "from-id";
        var toDefId = "to-id";
        var batchSize = 10;
        var maxJobs = 2;

        var fromDef = mock(ProcessDefinition.class);
        when(fromDef.id()).thenReturn(fromDefId);
        var toDef = mock(ProcessDefinition.class);
        when(toDef.id()).thenReturn(toDefId);

        var job = mock(Job.class);
        var command = EasyProcessMigrationCommand.of(fromDef, toDef, null, null);

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

        // First call returns 10, second returns 5 (less than batchSize, so loop stops)
        // Since maxJobs = 2, it will happen twice.
        when(processPersistence.updateDefinitionId(fromDefId, toDefId, batchSize))
                .thenReturn(10, 5, 10, 5);

        // When
        var result = handler.execute(job, command);

        // Then
        assertThat(result).containsEntry("Total migrated processes", 30L);
        verify(processPersistence, times(4)).updateDefinitionId(fromDefId, toDefId, batchSize);
    }

}
