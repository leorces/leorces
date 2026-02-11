package com.leorces.engine.activity.handler;

import com.leorces.engine.activity.behaviour.ActivityBehavior;
import com.leorces.engine.activity.behaviour.ActivityBehaviorResolver;
import com.leorces.engine.activity.command.DeleteActivityCommand;
import com.leorces.engine.activity.command.FindActivityCommand;
import com.leorces.engine.core.CommandDispatcher;
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
    private CommandDispatcher dispatcher;

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
        var behavior = mock(ActivityBehavior.class);

        when(dispatcher.execute(FindActivityCommand.byId(activityId))).thenReturn(activity);
        when(activity.type()).thenReturn(ActivityType.EXTERNAL_TASK);
        when(behaviorResolver.resolveBehavior(ActivityType.EXTERNAL_TASK)).thenReturn(behavior);

        // When
        handler.handle(command);

        // Then
        verify(behavior).delete(activity);
    }

    @Test
    @DisplayName("Should delete activity when it is passed in command")
    void handleDeleteSuccessWithActivityInCommand() {
        // Given
        var activity = mock(ActivityExecution.class);
        var command = DeleteActivityCommand.of(activity);
        var behavior = mock(ActivityBehavior.class);

        when(activity.type()).thenReturn(ActivityType.EXTERNAL_TASK);
        when(behaviorResolver.resolveBehavior(ActivityType.EXTERNAL_TASK)).thenReturn(behavior);

        // When
        handler.handle(command);

        // Then
        verify(behavior).delete(activity);
        verifyNoInteractions(dispatcher);
    }

}
