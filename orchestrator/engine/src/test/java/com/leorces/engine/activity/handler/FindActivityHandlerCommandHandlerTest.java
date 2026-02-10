package com.leorces.engine.activity.handler;

import com.leorces.engine.activity.command.FindActivityHandlerCommand;
import com.leorces.model.definition.ProcessDefinition;
import com.leorces.model.definition.activity.*;
import com.leorces.model.runtime.process.Process;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("FindActivityHandlerCommandHandler Tests")
class FindActivityHandlerCommandHandlerTest {

    private static final String ERROR_CODE = "error-123";
    private static final String ESCALATION_CODE = "escalation-456";
    private static final String SCOPE_ID = "scope-789";
    private static final String PROCESS_DEF_ID = "process-def-id";

    @Mock
    private Process process;

    @Mock
    private ProcessDefinition processDefinition;

    @InjectMocks
    private FindActivityHandlerCommandHandler handler;

    @Test
    @DisplayName("Should find error boundary event by code")
    void shouldFindErrorBoundaryEventByCode() {
        // Given
        var boundaryEvent = mock(TestErrorBoundaryEvent.class);
        setupMockActivity(boundaryEvent, ActivityType.ERROR_BOUNDARY_EVENT, SCOPE_ID, ERROR_CODE);

        when(process.definition()).thenReturn(processDefinition);
        when(process.definitionId()).thenReturn(PROCESS_DEF_ID);
        when(processDefinition.activities()).thenReturn(List.of(boundaryEvent));

        var command = FindActivityHandlerCommand.error(ERROR_CODE, SCOPE_ID, process);

        // When
        var result = handler.execute(command);

        // Then
        assertThat(result).isPresent().contains(boundaryEvent);
    }

    @Test
    @DisplayName("Should find escalation boundary event by code")
    void shouldFindEscalationBoundaryEventByCode() {
        // Given
        var boundaryEvent = mock(TestEscalationBoundaryEvent.class);
        setupMockEscalationActivity(boundaryEvent, ActivityType.ESCALATION_BOUNDARY_EVENT, SCOPE_ID, ESCALATION_CODE);

        when(process.definition()).thenReturn(processDefinition);
        when(process.definitionId()).thenReturn(PROCESS_DEF_ID);
        when(processDefinition.activities()).thenReturn(List.of(boundaryEvent));

        var command = FindActivityHandlerCommand.escalation(ESCALATION_CODE, SCOPE_ID, process);

        // When
        var result = handler.execute(command);

        // Then
        assertThat(result).isPresent().contains(boundaryEvent);
    }

    @Test
    @DisplayName("Should find error start event in subprocess scope")
    void shouldFindErrorStartEventInSubprocessScope() {
        // Given
        var subProcessId = "subprocess-1";
        var startEvent = mock(TestErrorStartEvent.class);
        var eventSubProcess = mock(ActivityDefinition.class);

        when(startEvent.type()).thenReturn(ActivityType.ERROR_START_EVENT);
        when(startEvent.errorCode()).thenReturn(ERROR_CODE);
        when(startEvent.parentId()).thenReturn(subProcessId);

        when(eventSubProcess.parentId()).thenReturn(SCOPE_ID);
        when(processDefinition.getActivityById(subProcessId)).thenReturn(Optional.of(eventSubProcess));

        when(process.definition()).thenReturn(processDefinition);
        when(process.definitionId()).thenReturn(PROCESS_DEF_ID);
        when(processDefinition.activities()).thenReturn(List.of(startEvent));

        var command = FindActivityHandlerCommand.error(ERROR_CODE, SCOPE_ID, process);

        // When
        var result = handler.execute(command);

        // Then
        assertThat(result).isPresent().contains(startEvent);
    }

    @Test
    @DisplayName("Should find process level error start event")
    void shouldFindProcessLevelErrorStartEvent() {
        // Given
        var startEvent = mock(TestErrorStartEvent.class);
        when(startEvent.type()).thenReturn(ActivityType.ERROR_START_EVENT);
        when(startEvent.errorCode()).thenReturn(ERROR_CODE);

        when(process.definition()).thenReturn(processDefinition);
        when(process.definitionId()).thenReturn(PROCESS_DEF_ID);
        when(processDefinition.activities()).thenReturn(List.of(startEvent));

        // Scope is same as process definition ID
        var command = FindActivityHandlerCommand.error(ERROR_CODE, PROCESS_DEF_ID, process);

        // When
        var result = handler.execute(command);

        // Then
        assertThat(result).isPresent().contains(startEvent);
    }

    @Test
    @DisplayName("Should return empty when no handler matches")
    void shouldReturnEmptyWhenNoHandlerMatches() {
        // Given
        when(process.definition()).thenReturn(processDefinition);
        when(process.definitionId()).thenReturn(PROCESS_DEF_ID);
        when(processDefinition.activities()).thenReturn(List.of());

        var command = FindActivityHandlerCommand.error("wrong-code", SCOPE_ID, process);

        // When
        var result = handler.execute(command);

        // Then
        assertThat(result).isEmpty();
    }

    private void setupMockActivity(TestErrorBoundaryEvent mock, ActivityType type, String attachedTo, String code) {
        when(mock.type()).thenReturn(type);
        when(mock.attachedToRef()).thenReturn(attachedTo);
        when(mock.errorCode()).thenReturn(code);
    }

    private void setupMockEscalationActivity(TestEscalationBoundaryEvent mock, ActivityType type, String attachedTo, String code) {
        when(mock.type()).thenReturn(type);
        when(mock.attachedToRef()).thenReturn(attachedTo);
        when(mock.escalationCode()).thenReturn(code);
    }

    // Helper interfaces for mocking multiple types
    interface TestErrorBoundaryEvent extends ActivityDefinition, BoundaryEventDefinition, ErrorActivityDefinition {
    }

    interface TestEscalationBoundaryEvent extends ActivityDefinition, BoundaryEventDefinition, EscalationActivityDefinition {
    }

    interface TestErrorStartEvent extends ActivityDefinition, ErrorActivityDefinition {
    }

}
