package com.leorces.engine.activity.handler;

import com.leorces.engine.activity.ActivityFactory;
import com.leorces.engine.activity.behaviour.ActivityBehavior;
import com.leorces.engine.activity.behaviour.ActivityBehaviorResolver;
import com.leorces.engine.activity.command.HandleActivityCompletionCommand;
import com.leorces.engine.activity.command.TerminateActivityCommand;
import com.leorces.engine.core.CommandDispatcher;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("TerminateActivityCommandHandler Tests")
class TerminateActivityCommandHandlerTest {

    private static final String ACTIVITY_ID = "activity-id";
    private static final String PROCESS_ID = "process-id";
    private static final String DEFINITION_ID = "definition-id";

    @Mock
    private ActivityBehaviorResolver behaviorResolver;

    @Mock
    private ActivityFactory activityFactory;

    @Mock
    private CommandDispatcher dispatcher;

    @Mock
    private ActivityBehavior activityBehavior;

    @Mock
    private ActivityExecution activityExecution;

    @InjectMocks
    private TerminateActivityCommandHandler handler;

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
        // Given & When
        var commandType = handler.getCommandType();

        // Then
        assertThat(commandType).isEqualTo(TerminateActivityCommand.class);
    }

    @Test
    @DisplayName("Should terminate activity with provided activity and dispatch completion command")
    void shouldTerminateActivityWithProvidedActivityAndDispatchCompletionCommand() {
        // Given
        var command = TerminateActivityCommand.of(activityExecution, false);

        // When
        handler.handle(command);

        // Then
        verify(behaviorResolver).resolveBehavior(ActivityType.EXTERNAL_TASK);
        verify(activityBehavior).terminate(activityExecution);
        verify(dispatcher).dispatch(any(HandleActivityCompletionCommand.class));
    }

    @Test
    @DisplayName("Should terminate activity with interruption and not dispatch completion command")
    void shouldTerminateActivityWithInterruptionAndNotDispatchCompletionCommand() {
        // Given
        var command = TerminateActivityCommand.of(activityExecution, true);

        // When
        handler.handle(command);

        // Then
        verify(behaviorResolver).resolveBehavior(ActivityType.EXTERNAL_TASK);
        verify(activityBehavior).terminate(activityExecution);
        verify(dispatcher, never()).dispatch(any(HandleActivityCompletionCommand.class));
    }

    @Test
    @DisplayName("Should terminate activity with activity ID")
    void shouldTerminateActivityWithActivityId() {
        // Given
        var command = TerminateActivityCommand.of(ACTIVITY_ID);
        when(activityFactory.getById(ACTIVITY_ID)).thenReturn(activityExecution);

        // When
        handler.handle(command);

        // Then
        verify(activityFactory).getById(ACTIVITY_ID);
        verify(behaviorResolver).resolveBehavior(ActivityType.EXTERNAL_TASK);
        verify(activityBehavior).terminate(activityExecution);
        verify(dispatcher).dispatch(any(HandleActivityCompletionCommand.class));
    }

    @Test
    @DisplayName("Should handle different activity types")
    void shouldHandleDifferentActivityTypes() {
        // Given
        when(activityExecution.type()).thenReturn(ActivityType.RECEIVE_TASK);
        when(behaviorResolver.resolveBehavior(ActivityType.RECEIVE_TASK)).thenReturn(activityBehavior);
        var command = TerminateActivityCommand.of(activityExecution, false);

        // When
        handler.handle(command);

        // Then
        verify(behaviorResolver).resolveBehavior(ActivityType.RECEIVE_TASK);
        verify(activityBehavior).terminate(activityExecution);
        verify(dispatcher).dispatch(any(HandleActivityCompletionCommand.class));
    }

}