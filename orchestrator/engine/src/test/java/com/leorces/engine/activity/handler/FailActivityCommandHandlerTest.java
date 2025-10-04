package com.leorces.engine.activity.handler;

import com.leorces.engine.activity.ActivityFactory;
import com.leorces.engine.activity.behaviour.ActivityBehavior;
import com.leorces.engine.activity.behaviour.ActivityBehaviorResolver;
import com.leorces.engine.activity.command.FailActivityCommand;
import com.leorces.engine.core.CommandDispatcher;
import com.leorces.engine.exception.ExecutionException;
import com.leorces.engine.process.command.IncidentProcessCommand;
import com.leorces.model.definition.activity.ActivityType;
import com.leorces.model.runtime.activity.ActivityExecution;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("FailActivityCommandHandler Tests")
class FailActivityCommandHandlerTest {

    private static final String ACTIVITY_ID = "activity-id";
    private static final String PROCESS_ID = "process-id";
    private static final String DEFINITION_ID = "definition-id";

    @Mock
    private ActivityFactory activityFactory;

    @Mock
    private ActivityBehaviorResolver behaviorResolver;

    @Mock
    private CommandDispatcher dispatcher;

    @Mock
    private ActivityBehavior activityBehavior;

    @Mock
    private ActivityExecution activityExecution;

    @InjectMocks
    private FailActivityCommandHandler handler;

    @BeforeEach
    void setUp() {
        when(activityExecution.id()).thenReturn(ACTIVITY_ID);
        when(activityExecution.processId()).thenReturn(PROCESS_ID);
        when(activityExecution.definitionId()).thenReturn(DEFINITION_ID);
        when(activityExecution.type()).thenReturn(ActivityType.EXTERNAL_TASK);
        when(behaviorResolver.resolveBehavior(ActivityType.EXTERNAL_TASK)).thenReturn(activityBehavior);
    }

    @Test
    @DisplayName("Should return correct command type")
    void shouldReturnCorrectCommandType() {
        assertThat(handler.getCommandType()).isEqualTo(FailActivityCommand.class);
    }

    @Test
    @DisplayName("Should fail activity successfully when behavior returns true")
    void shouldFailActivitySuccessfullyWhenBehaviorReturnsTrue() {
        var command = FailActivityCommand.of(activityExecution);

        when(activityBehavior.fail(activityExecution)).thenReturn(true);

        handler.handle(command);

        verify(behaviorResolver).resolveBehavior(ActivityType.EXTERNAL_TASK);
        verify(activityBehavior).fail(activityExecution);
        verify(dispatcher).dispatchAsync(any(IncidentProcessCommand.class));
        verifyNoMoreInteractions(dispatcher);
    }

    @Test
    @DisplayName("Should not dispatch incident command when behavior returns false")
    void shouldNotDispatchIncidentCommandWhenBehaviorReturnsFalse() {
        var command = FailActivityCommand.of(activityExecution);

        when(activityBehavior.fail(activityExecution)).thenReturn(false);

        handler.handle(command);

        verify(behaviorResolver).resolveBehavior(ActivityType.EXTERNAL_TASK);
        verify(activityBehavior).fail(activityExecution);
        verify(dispatcher, never()).dispatchAsync(any());
    }

    @Test
    @DisplayName("Should throw ExecutionException when activity not found")
    void shouldThrowExecutionExceptionWhenActivityNotFound() {
        var command = FailActivityCommand.of(ACTIVITY_ID);

        when(activityFactory.getById(ACTIVITY_ID)).thenThrow(new ExecutionException("Activity not found: " + ACTIVITY_ID));

        assertThatThrownBy(() -> handler.handle(command))
                .isInstanceOf(ExecutionException.class)
                .hasMessage("Activity not found: " + ACTIVITY_ID);

        verify(activityFactory).getById(ACTIVITY_ID);
        verifyNoInteractions(dispatcher, behaviorResolver, activityBehavior);
    }

    @Test
    @DisplayName("Should handle different activity types")
    void shouldHandleDifferentActivityTypes() {
        var command = FailActivityCommand.of(activityExecution);

        when(activityExecution.type()).thenReturn(ActivityType.RECEIVE_TASK);
        when(behaviorResolver.resolveBehavior(ActivityType.RECEIVE_TASK)).thenReturn(activityBehavior);
        when(activityBehavior.fail(activityExecution)).thenReturn(true);

        handler.handle(command);

        verify(behaviorResolver).resolveBehavior(ActivityType.RECEIVE_TASK);
        verify(activityBehavior).fail(activityExecution);
        verify(dispatcher).dispatchAsync(any(IncidentProcessCommand.class));
    }

}
