package com.leorces.engine.activity.handler;

import com.leorces.engine.activity.ActivityFactory;
import com.leorces.engine.activity.behaviour.ActivityBehavior;
import com.leorces.engine.activity.behaviour.ActivityBehaviorResolver;
import com.leorces.engine.activity.command.RetryActivityCommand;
import com.leorces.engine.exception.ExecutionException;
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
@DisplayName("RetryActivityCommandHandler Tests")
class RetryActivityCommandHandlerTest {

    private static final String ACTIVITY_ID = "activity-id";
    private static final String DEFINITION_ID = "definition-id";
    private static final String PROCESS_ID = "process-id";

    @Mock
    private ActivityBehaviorResolver behaviorResolver;

    @Mock
    private ActivityFactory activityFactory;

    @Mock
    private ActivityBehavior activityBehavior;

    @Mock
    private ActivityExecution activityExecution;

    @InjectMocks
    private RetryActivityCommandHandler handler;

    @BeforeEach
    void setUp() {
        when(activityExecution.id()).thenReturn(ACTIVITY_ID);
        when(activityExecution.definitionId()).thenReturn(DEFINITION_ID);
        when(activityExecution.processId()).thenReturn(PROCESS_ID);
        when(activityExecution.type()).thenReturn(ActivityType.EXTERNAL_TASK);
        when(behaviorResolver.resolveBehavior(ActivityType.EXTERNAL_TASK)).thenReturn(activityBehavior);
    }

    @Test
    @DisplayName("Should return correct command type")
    void shouldReturnCorrectCommandType() {
        assertThat(handler.getCommandType()).isEqualTo(RetryActivityCommand.class);
    }

    @Test
    @DisplayName("Should retry activity successfully when activity provided in command")
    void shouldRetryActivityWhenActivityProvided() {
        var command = RetryActivityCommand.of(activityExecution);

        handler.handle(command);

        verify(behaviorResolver).resolveBehavior(ActivityType.EXTERNAL_TASK);
        verify(activityBehavior).retry(activityExecution);
    }

    @Test
    @DisplayName("Should retry activity successfully when only activityId provided")
    void shouldRetryActivityWhenActivityIdProvided() {
        var command = RetryActivityCommand.of(ACTIVITY_ID);

        when(activityFactory.getById(ACTIVITY_ID)).thenReturn(activityExecution);

        handler.handle(command);

        verify(activityFactory).getById(ACTIVITY_ID);
        verify(behaviorResolver).resolveBehavior(ActivityType.EXTERNAL_TASK);
        verify(activityBehavior).retry(activityExecution);
    }

    @Test
    @DisplayName("Should throw ExecutionException when activity not found by factory")
    void shouldThrowExceptionWhenActivityNotFound() {
        var command = RetryActivityCommand.of(ACTIVITY_ID);

        when(activityFactory.getById(ACTIVITY_ID)).thenThrow(new ExecutionException("Activity not found: " + ACTIVITY_ID));

        assertThatThrownBy(() -> handler.handle(command))
                .isInstanceOf(ExecutionException.class)
                .hasMessage("Activity not found: " + ACTIVITY_ID);

        verify(activityFactory).getById(ACTIVITY_ID);
        verifyNoInteractions(behaviorResolver, activityBehavior);
    }

    @Test
    @DisplayName("Should handle different activity types")
    void shouldHandleDifferentActivityTypes() {
        var command = RetryActivityCommand.of(activityExecution);

        when(activityExecution.type()).thenReturn(ActivityType.EXCLUSIVE_GATEWAY);
        when(behaviorResolver.resolveBehavior(ActivityType.EXCLUSIVE_GATEWAY)).thenReturn(activityBehavior);

        handler.handle(command);

        verify(behaviorResolver).resolveBehavior(ActivityType.EXCLUSIVE_GATEWAY);
        verify(activityBehavior).retry(activityExecution);
    }

}
