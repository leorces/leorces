package com.leorces.engine.process.handler;

import com.leorces.engine.core.CommandDispatcher;
import com.leorces.engine.process.command.TerminateProcessCommand;
import com.leorces.engine.service.process.ProcessMetrics;
import com.leorces.model.runtime.process.Process;
import com.leorces.model.runtime.process.ProcessState;
import com.leorces.persistence.ActivityPersistence;
import com.leorces.persistence.ProcessPersistence;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TerminateProcessCommandHandler Tests")
class TerminateProcessCommandHandlerTest {

    private static final String PROCESS_ID = "process-1";

    @Mock
    private ProcessPersistence processPersistence;

    @Mock
    private ActivityPersistence activityPersistence;

    @Mock
    private ProcessMetrics processMetrics;

    @Mock
    private CommandDispatcher dispatcher;

    @InjectMocks
    private TerminateProcessCommandHandler handler;

    private Process activeProcess;
    private Process terminatedProcess;

    @BeforeEach
    void setUp() {
        activeProcess = Process.builder()
                .id(PROCESS_ID)
                .businessKey("bk1")
                .definition(null)
                .state(ProcessState.ACTIVE)
                .variables(List.of())
                .build();

        terminatedProcess = activeProcess.toBuilder()
                .state(ProcessState.TERMINATED)
                .build();
    }

    @Test
    @DisplayName("Handle should terminate an active process and record metrics")
    void handleShouldTerminateActiveProcess() {
        // Given
        var command = TerminateProcessCommand.of(PROCESS_ID);
        when(processPersistence.findById(PROCESS_ID)).thenReturn(Optional.of(activeProcess));
        when(activityPersistence.findActive(PROCESS_ID)).thenReturn(List.of());

        // When
        handler.handle(command);

        // Then
        verify(processPersistence).terminate(activeProcess.id());
        verify(processMetrics).recordProcessTerminatedMetric(activeProcess);
        verify(dispatcher).dispatch(any());
    }

    @Test
    @DisplayName("Handle should do nothing if process is already terminated")
    void handleShouldDoNothingIfProcessAlreadyTerminated() {
        // Given
        var command = TerminateProcessCommand.of(PROCESS_ID);
        when(processPersistence.findById(PROCESS_ID)).thenReturn(Optional.of(terminatedProcess));

        // When
        handler.handle(command);

        // Then
        verifyNoInteractions(activityPersistence, processMetrics, dispatcher);
        verify(processPersistence, never()).terminate(terminatedProcess.id());
    }

    @Test
    @DisplayName("Handle should throw if process not found")
    void handleShouldThrowIfProcessNotFound() {
        // Given
        var command = TerminateProcessCommand.of(PROCESS_ID);
        when(processPersistence.findById(PROCESS_ID)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(
                RuntimeException.class,
                () -> handler.handle(command)
        );

        verifyNoInteractions(activityPersistence, processMetrics, dispatcher);
    }

}
