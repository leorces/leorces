package com.leorces.engine.process.handler;

import com.leorces.engine.process.command.RunProcessCommand;
import com.leorces.engine.service.process.ProcessFactory;
import com.leorces.engine.service.process.ProcessRuntimeService;
import com.leorces.model.definition.ProcessDefinition;
import com.leorces.model.runtime.activity.ActivityExecution;
import com.leorces.model.runtime.process.Process;
import com.leorces.model.runtime.process.ProcessState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("RunProcessCommandHandler Tests")
class RunProcessCommandHandlerTest {

    private static final String PROCESS_ID = "process-1";
    private static final String DEFINITION_ID = "definition-1";
    private static final String DEFINITION_KEY = "def-key";

    @Mock
    private ProcessRuntimeService processRuntimeService;

    @Mock
    private ProcessFactory processFactory;

    @InjectMocks
    private RunProcessCommandHandler handler;

    private Process process;
    private ActivityExecution callActivity;

    @BeforeEach
    void setUp() {
        callActivity = ActivityExecution.builder()
                .id("activity-1")
                .build();

        var definition = ProcessDefinition.builder()
                .id(DEFINITION_ID)
                .key(DEFINITION_KEY)
                .name("Test Definition")
                .version(1)
                .activities(List.of())
                .build();

        process = Process.builder()
                .id(PROCESS_ID)
                .parentId(null)
                .rootProcessId(null)
                .businessKey("bk1")
                .variables(List.of())
                .state(ProcessState.ACTIVE)
                .definition(definition)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .startedAt(LocalDateTime.now())
                .completedAt(null)
                .build();
    }

    @Test
    @DisplayName("Should start process returned by factory")
    void handleShouldStartProcess() {
        // Given
        var command = RunProcessCommand.of(callActivity);
        when(processFactory.createByCallActivity(callActivity)).thenReturn(process);

        // When
        handler.handle(command);

        // Then
        verify(processFactory).createByCallActivity(callActivity);
        verify(processRuntimeService).start(process);
    }

    @Test
    @DisplayName("Should handle multiple processes with different call activities")
    void handleShouldStartDifferentProcesses() {
        // Given
        var anotherActivity = ActivityExecution.builder().id("activity-2").build();
        var process2 = process.toBuilder().id("process-2").build();

        var command1 = RunProcessCommand.of(callActivity);
        var command2 = RunProcessCommand.of(anotherActivity);

        when(processFactory.createByCallActivity(callActivity)).thenReturn(process);
        when(processFactory.createByCallActivity(anotherActivity)).thenReturn(process2);

        // When
        handler.handle(command1);
        handler.handle(command2);

        // Then
        verify(processFactory).createByCallActivity(callActivity);
        verify(processFactory).createByCallActivity(anotherActivity);
        verify(processRuntimeService).start(process);
        verify(processRuntimeService).start(process2);
    }

}
