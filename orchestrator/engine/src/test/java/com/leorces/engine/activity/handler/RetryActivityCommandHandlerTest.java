package com.leorces.engine.activity.handler;

import com.leorces.engine.activity.behaviour.ActivityBehavior;
import com.leorces.engine.activity.behaviour.ActivityBehaviorResolver;
import com.leorces.engine.activity.command.RetryActivityCommand;
import com.leorces.engine.service.activity.ActivityFactory;
import com.leorces.model.definition.activity.ActivityType;
import com.leorces.model.runtime.activity.ActivityExecution;
import com.leorces.model.runtime.process.Process;
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

        var process = mock(Process.class);
        when(process.isInTerminalState()).thenReturn(false);
        when(activityExecution.process()).thenReturn(process);

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
        var activityFromFactory = mock(ActivityExecution.class);
        when(activityFromFactory.id()).thenReturn(ACTIVITY_ID);
        when(activityFromFactory.definitionId()).thenReturn(DEFINITION_ID);
        when(activityFromFactory.type()).thenReturn(ActivityType.EXTERNAL_TASK);

        var process = mock(Process.class);
        when(process.isInTerminalState()).thenReturn(false);
        when(activityFromFactory.process()).thenReturn(process);

        when(activityFactory.getById(ACTIVITY_ID)).thenReturn(activityFromFactory);
        when(behaviorResolver.resolveBehavior(ActivityType.EXTERNAL_TASK)).thenReturn(activityBehavior);

        handler.handle(command);

        verify(activityFactory).getById(ACTIVITY_ID);
        verify(behaviorResolver).resolveBehavior(ActivityType.EXTERNAL_TASK);
        verify(activityBehavior).retry(activityFromFactory);
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

    @Test
    @DisplayName("Should not retry activity in terminal state")
    void shouldNotRetryActivityInTerminalState() {
        when(activityExecution.isInTerminalState()).thenReturn(true);

        var command = RetryActivityCommand.of(activityExecution);

        handler.handle(command);

        verifyNoInteractions(behaviorResolver, activityBehavior);
    }

    @Test
    @DisplayName("Should retry async activity even if process is in terminal state")
    void shouldRetryAsyncActivityIfProcessTerminal() {
        when(activityExecution.isAsync()).thenReturn(true);

        var process = mock(Process.class);
        when(process.isInTerminalState()).thenReturn(true);
        when(activityExecution.process()).thenReturn(process);

        var command = RetryActivityCommand.of(activityExecution);

        handler.handle(command);

        verify(behaviorResolver).resolveBehavior(ActivityType.EXTERNAL_TASK);
        verify(activityBehavior).retry(activityExecution);
    }

    @Test
    @DisplayName("Should not retry non-async activity if process is in terminal state")
    void shouldNotRetryNonAsyncActivityIfProcessTerminal() {
        when(activityExecution.isAsync()).thenReturn(false);

        var process = mock(Process.class);
        when(process.isInTerminalState()).thenReturn(true);
        when(activityExecution.process()).thenReturn(process);

        var command = RetryActivityCommand.of(activityExecution);

        handler.handle(command);

        verifyNoInteractions(behaviorResolver, activityBehavior);
    }

}
