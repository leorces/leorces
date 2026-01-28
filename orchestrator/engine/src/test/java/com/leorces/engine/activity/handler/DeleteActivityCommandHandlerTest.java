package com.leorces.engine.activity.handler;

import com.leorces.engine.activity.behaviour.ActivityBehavior;
import com.leorces.engine.activity.behaviour.ActivityBehaviorResolver;
import com.leorces.engine.activity.command.DeleteActivityCommand;
import com.leorces.engine.service.activity.ActivityFactory;
import com.leorces.model.definition.activity.ActivityType;
import com.leorces.model.runtime.activity.ActivityExecution;
import com.leorces.model.runtime.process.Process;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeleteActivityCommandHandlerTest {

    @Mock
    private ActivityBehaviorResolver behaviorResolver;

    @Mock
    private ActivityFactory activityFactory;

    @InjectMocks
    private DeleteActivityCommandHandler handler;

    @Test
    @DisplayName("Should return correct command type")
    void getCommandType() {
        // When
        var result = handler.getCommandType();

        // Then
        assertThat(result).isEqualTo(DeleteActivityCommand.class);
    }

    @Test
    @DisplayName("Should delete activity when it is not in terminal state and process is active")
    void handleDeleteSuccess() {
        // Given
        var activityId = "activity-1";
        var command = DeleteActivityCommand.of(activityId);
        var activity = mock(ActivityExecution.class);
        var process = mock(Process.class);
        var behavior = mock(ActivityBehavior.class);

        when(activityFactory.getById(activityId)).thenReturn(activity);
        when(activity.isInTerminalState()).thenReturn(false);
        when(activity.process()).thenReturn(process);
        when(process.isInTerminalState()).thenReturn(false);
        when(activity.type()).thenReturn(ActivityType.EXTERNAL_TASK);
        when(behaviorResolver.resolveBehavior(ActivityType.EXTERNAL_TASK)).thenReturn(behavior);

        // When
        handler.handle(command);

        // Then
        verify(behavior).delete(activity);
    }

    @Test
    @DisplayName("Should delete activity when it is not in terminal state and process is in terminal state but activity is async")
    void handleDeleteAsyncActivityInTerminalProcess() {
        // Given
        var activityId = "activity-1";
        var command = DeleteActivityCommand.of(activityId);
        var activity = mock(ActivityExecution.class);
        var process = mock(Process.class);
        var behavior = mock(ActivityBehavior.class);

        when(activityFactory.getById(activityId)).thenReturn(activity);
        when(activity.isInTerminalState()).thenReturn(false);
        when(activity.process()).thenReturn(process);
        when(process.isInTerminalState()).thenReturn(true);
        when(activity.isAsync()).thenReturn(true);
        when(activity.type()).thenReturn(ActivityType.EXTERNAL_TASK);
        when(behaviorResolver.resolveBehavior(ActivityType.EXTERNAL_TASK)).thenReturn(behavior);

        // When
        handler.handle(command);

        // Then
        verify(behavior).delete(activity);
    }

    @Test
    @DisplayName("Should not delete activity when it is in terminal state")
    void handleActivityInTerminalState() {
        // Given
        var activityId = "activity-1";
        var command = DeleteActivityCommand.of(activityId);
        var activity = mock(ActivityExecution.class);

        when(activityFactory.getById(activityId)).thenReturn(activity);
        when(activity.isInTerminalState()).thenReturn(true);

        // When
        handler.handle(command);

        // Then
        verifyNoInteractions(behaviorResolver);
    }

    @Test
    @DisplayName("Should not delete activity when process is in terminal state and activity is not async")
    void handleProcessInTerminalState() {
        // Given
        var activityId = "activity-1";
        var command = DeleteActivityCommand.of(activityId);
        var activity = mock(ActivityExecution.class);
        var process = mock(Process.class);

        when(activityFactory.getById(activityId)).thenReturn(activity);
        when(activity.isInTerminalState()).thenReturn(false);
        when(activity.process()).thenReturn(process);
        when(process.isInTerminalState()).thenReturn(true);
        when(activity.isAsync()).thenReturn(false);

        // When
        handler.handle(command);

        // Then
        verifyNoInteractions(behaviorResolver);
    }

}
