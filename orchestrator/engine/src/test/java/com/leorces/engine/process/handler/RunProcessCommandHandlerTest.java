package com.leorces.engine.process.handler;

import com.leorces.engine.core.CommandDispatcher;
import com.leorces.engine.process.command.CreateProcessCommand;
import com.leorces.engine.process.command.RecordProcessMetricCommand;
import com.leorces.engine.process.command.RunProcessCommand;
import com.leorces.model.definition.ProcessDefinition;
import com.leorces.model.definition.activity.ActivityDefinition;
import com.leorces.model.definition.activity.ActivityType;
import com.leorces.model.runtime.activity.ActivityExecution;
import com.leorces.model.runtime.process.Process;
import com.leorces.model.runtime.process.ProcessState;
import com.leorces.persistence.ProcessPersistence;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.LocalDateTime;
import java.util.List;

import static com.leorces.engine.constants.MetricConstants.PROCESS_STARTED;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("RunProcessCommandHandler Tests")
class RunProcessCommandHandlerTest {

    private static final String PROCESS_ID = "process-1";
    private static final String DEFINITION_ID = "definition-1";
    private static final String DEFINITION_KEY = "def-key";

    @Mock
    private ProcessPersistence processPersistence;

    @Mock
    private CommandDispatcher dispatcher;

    @InjectMocks
    private RunProcessCommandHandler handler;

    private Process process;
    private ActivityExecution callActivity;

    @BeforeEach
    void setUp() {
        callActivity = ActivityExecution.builder()
                .id("activity-1")
                .build();

        var startActivity = mock(ActivityDefinition.class);
        when(startActivity.id()).thenReturn("start");
        when(startActivity.type()).thenReturn(ActivityType.START_EVENT);
        when(startActivity.parentId()).thenReturn(null);
        var definition = ProcessDefinition.builder()
                .id(DEFINITION_ID)
                .key(DEFINITION_KEY)
                .name("Test Definition")
                .version(1)
                .activities(List.of(startActivity))
                .build();

        process = Process.builder()
                .id(PROCESS_ID)
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
        var command = RunProcessCommand.byCallActivity(callActivity);
        when(dispatcher.execute(any(CreateProcessCommand.class))).thenReturn(process);
        when(processPersistence.run(process)).thenReturn(process);

        // When
        handler.handle(command);

        // Then
        verify(dispatcher).execute(any(CreateProcessCommand.class));
        verify(processPersistence).run(process);
        verify(dispatcher).dispatchAsync(RecordProcessMetricCommand.of(PROCESS_STARTED, process));
    }

    @Test
    @DisplayName("Should handle multiple processes with different call activities")
    void handleShouldStartDifferentProcesses() {
        // Given
        var anotherActivity = ActivityExecution.builder().id("activity-2").build();
        var process2 = process.toBuilder().id("process-2").build();

        var command1 = RunProcessCommand.byCallActivity(callActivity);
        var command2 = RunProcessCommand.byCallActivity(anotherActivity);

        when(dispatcher.execute(any(CreateProcessCommand.class))).thenReturn(process, process2);
        when(processPersistence.run(process)).thenReturn(process);
        when(processPersistence.run(process2)).thenReturn(process2);

        // When
        handler.handle(command1);
        handler.handle(command2);

        // Then
        verify(dispatcher, times(2)).execute(any(CreateProcessCommand.class));
        verify(processPersistence).run(process);
        verify(processPersistence).run(process2);
        verify(dispatcher).dispatchAsync(RecordProcessMetricCommand.of(PROCESS_STARTED, process));
        verify(dispatcher).dispatchAsync(RecordProcessMetricCommand.of(PROCESS_STARTED, process2));
    }

}
