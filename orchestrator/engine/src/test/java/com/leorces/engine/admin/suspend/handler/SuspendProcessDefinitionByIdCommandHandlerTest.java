package com.leorces.engine.admin.suspend.handler;

import com.leorces.engine.admin.suspend.command.SuspendProcessDefinitionByIdCommand;
import com.leorces.engine.configuration.properties.job.SuspendProcessDefinitionProperties;
import com.leorces.engine.service.TaskExecutorService;
import com.leorces.model.definition.ProcessDefinition;
import com.leorces.model.job.Job;
import com.leorces.persistence.AdminPersistence;
import com.leorces.persistence.DefinitionPersistence;
import com.leorces.persistence.JobPersistence;
import com.leorces.persistence.ProcessPersistence;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SuspendProcessDefinitionByIdCommandHandler Tests")
class SuspendProcessDefinitionByIdCommandHandlerTest {

    private static final String OUTPUT_KEY = "Total suspended processes";
    private static final String PROCESS_DEFINITION_KEY = "testKey";
    private static final String PROCESS_DEFINITION_ID = "testId";
    private static final int PROCESS_DEFINITION_VERSION = 1;

    @Mock
    private JobPersistence jobPersistence;
    @Mock
    private AdminPersistence adminPersistence;
    @Mock
    private ProcessPersistence processPersistence;
    @Mock
    private DefinitionPersistence definitionPersistence;
    @Mock
    private TaskExecutorService taskExecutor;
    @Mock
    private SuspendProcessDefinitionProperties properties;

    @InjectMocks
    private SuspendProcessDefinitionByIdCommandHandler handler;

    @Test
    @DisplayName("should suspend process definition and its processes by id")
    void shouldSuspendById() throws Exception {
        // Given
        var batchSize = 10;
        var maxJobs = 1;
        var job = mock(Job.class);
        var input = Map.<String, Object>of();
        var command = new SuspendProcessDefinitionByIdCommand(PROCESS_DEFINITION_KEY, PROCESS_DEFINITION_VERSION, input);
        var processDefinition = mock(ProcessDefinition.class);

        when(processDefinition.id()).thenReturn(PROCESS_DEFINITION_ID);
        when(definitionPersistence.findByKeyAndVersion(PROCESS_DEFINITION_KEY, PROCESS_DEFINITION_VERSION))
                .thenReturn(Optional.of(processDefinition));

        when(properties.batchSize()).thenReturn(batchSize);
        when(properties.maxJobs()).thenReturn(maxJobs);

        mockExecutionInfrastructure();

        // 1st batch returns 10, 2nd returns 0
        when(processPersistence.suspendByDefinitionId(PROCESS_DEFINITION_ID, batchSize))
                .thenReturn(10, 0);

        // When
        var result = handler.execute(job, command);

        // Then
        assertThat(result).containsEntry(OUTPUT_KEY, 10L);
        verify(definitionPersistence).suspendById(PROCESS_DEFINITION_ID);
        verify(processPersistence, times(2)).suspendByDefinitionId(PROCESS_DEFINITION_ID, batchSize);
    }

    @Test
    @DisplayName("should return correct command type")
    void shouldReturnCorrectCommandType() {
        // When & Then
        assertThat(handler.getCommandType()).isEqualTo(SuspendProcessDefinitionByIdCommand.class);
    }

    private void mockExecutionInfrastructure() throws Exception {
        when(taskExecutor.supplyAsync(any())).thenAnswer(invocation -> {
            Callable<Long> callable = invocation.getArgument(0);
            return CompletableFuture.completedFuture(callable.call());
        });

        when(adminPersistence.execute(any())).thenAnswer(invocation -> {
            Supplier<Integer> supplier = invocation.getArgument(0);
            return supplier.get();
        });
    }

}
