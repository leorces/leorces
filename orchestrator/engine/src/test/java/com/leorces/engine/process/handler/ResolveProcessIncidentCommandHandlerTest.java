package com.leorces.engine.process.handler;

import com.leorces.engine.core.CommandDispatcher;
import com.leorces.engine.exception.process.ProcessNotFoundException;
import com.leorces.engine.process.command.ResolveProcessIncidentCommand;
import com.leorces.engine.service.process.ProcessMetrics;
import com.leorces.model.runtime.activity.ActivityExecution;
import com.leorces.model.runtime.activity.ActivityState;
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

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ResolveProcessIncidentCommandHandler Tests")
class ResolveProcessIncidentCommandHandlerTest {

    private static final String PROCESS_ID = "process-1";

    @Mock
    private ProcessPersistence processPersistence;

    @Mock
    private ActivityPersistence activityPersistence;

    @Mock
    private CommandDispatcher dispatcher;

    @Mock
    private ProcessMetrics processMetrics;

    @InjectMocks
    private ResolveProcessIncidentCommandHandler handler;

    private Process incidentProcess;
    private Process activeProcess;

    @BeforeEach
    void setUp() {
        incidentProcess = Process.builder()
                .id(PROCESS_ID)
                .parentId("parent-1")
                .businessKey("bk1")
                .state(ProcessState.INCIDENT)
                .variables(List.of())
                .build();

        activeProcess = Process.builder()
                .id(PROCESS_ID)
                .parentId("parent-1")
                .businessKey("bk1")
                .state(ProcessState.ACTIVE)
                .variables(List.of())
                .build();
    }

    @Test
    @DisplayName("Should resolve incident if process is in INCIDENT state and no failed activities")
    void handleShouldResolveIncident() {
        // Given
        var command = new ResolveProcessIncidentCommand(PROCESS_ID);
        when(processPersistence.findById(PROCESS_ID)).thenReturn(Optional.of(incidentProcess));
        when(activityPersistence.isAnyFailed(PROCESS_ID)).thenReturn(false);

        var mockActivity = ActivityExecution.builder()
                .id(PROCESS_ID)
                .process(incidentProcess)
                .state(ActivityState.FAILED)
                .build();

        when(activityPersistence.findById(PROCESS_ID)).thenReturn(Optional.of(mockActivity));

        // When
        handler.handle(command);

        // Then
        verify(processPersistence).changeState(PROCESS_ID, ProcessState.ACTIVE);
        verify(processMetrics).recordProcessRecoveredMetrics(incidentProcess);
        verify(activityPersistence).changeState(PROCESS_ID, ActivityState.ACTIVE);

        verify(dispatcher).dispatch(argThat(cmd ->
                cmd instanceof ResolveProcessIncidentCommand(String processId) &&
                        PROCESS_ID.equals(processId)
        ));
    }

    @Test
    @DisplayName("Should do nothing if process is not in INCIDENT state")
    void handleShouldDoNothingIfNotIncident() {
        // Given
        var command = new ResolveProcessIncidentCommand(PROCESS_ID);
        when(processPersistence.findById(PROCESS_ID)).thenReturn(Optional.of(activeProcess));

        // When
        handler.handle(command);

        // Then
        verifyNoInteractions(processMetrics, activityPersistence);
        verify(processPersistence, never()).changeState(PROCESS_ID, ProcessState.ACTIVE);
    }

    @Test
    @DisplayName("Should do nothing if any activity is failed")
    void handleShouldDoNothingIfAnyActivityFailed() {
        // Given
        var command = new ResolveProcessIncidentCommand(PROCESS_ID);
        when(processPersistence.findById(PROCESS_ID)).thenReturn(Optional.of(incidentProcess));
        when(activityPersistence.isAnyFailed(PROCESS_ID)).thenReturn(true);

        // When
        handler.handle(command);

        // Then
        verifyNoInteractions(processMetrics);
        verify(processPersistence, never()).changeState(PROCESS_ID, ProcessState.ACTIVE);
        verify(activityPersistence, never()).changeState(anyString(), any());
    }

    @Test
    @DisplayName("Should throw ProcessNotFoundException if process not found")
    void handleShouldThrowIfProcessNotFound() {
        // Given
        var command = new ResolveProcessIncidentCommand("unknown-id");
        when(processPersistence.findById("unknown-id")).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> handler.handle(command))
                .isInstanceOf(ProcessNotFoundException.class);
    }

}
