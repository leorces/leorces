package com.leorces.engine.activity.handler;

import com.leorces.engine.activity.behaviour.ActivityBehavior;
import com.leorces.engine.activity.behaviour.ActivityBehaviorResolver;
import com.leorces.engine.activity.command.DeleteAllActivitiesCommand;
import com.leorces.engine.service.TaskExecutorService;
import com.leorces.model.definition.activity.ActivityType;
import com.leorces.model.runtime.activity.ActivityExecution;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeleteAllActivitiesCommandHandlerTest {

    @Mock
    private ActivityBehaviorResolver behaviorResolver;

    @Mock
    private TaskExecutorService taskExecutor;

    @InjectMocks
    private DeleteAllActivitiesCommandHandler handler;

    @Test
    @DisplayName("Should return correct command type")
    void getCommandType() {
        // When
        var result = handler.getCommandType();

        // Then
        assertThat(result).isEqualTo(DeleteAllActivitiesCommand.class);
    }

    @Test
    @DisplayName("Should delete all activities asynchronously")
    void handleSuccess() {
        // Given
        var activity1 = mock(ActivityExecution.class);
        var activity2 = mock(ActivityExecution.class);
        var command = DeleteAllActivitiesCommand.of(List.of(activity1, activity2));

        var behavior1 = mock(ActivityBehavior.class);
        var behavior2 = mock(ActivityBehavior.class);

        when(activity1.type()).thenReturn(ActivityType.EXTERNAL_TASK);
        when(activity2.type()).thenReturn(ActivityType.SEND_TASK);

        when(behaviorResolver.resolveBehavior(ActivityType.EXTERNAL_TASK)).thenReturn(behavior1);
        when(behaviorResolver.resolveBehavior(ActivityType.SEND_TASK)).thenReturn(behavior2);

        // Mock taskExecutor to execute immediately
        when(taskExecutor.runAsync(any(Runnable.class))).thenAnswer(invocation -> {
            Runnable runnable = invocation.getArgument(0);
            runnable.run();
            return CompletableFuture.completedFuture(null);
        });

        // When
        handler.handle(command);

        // Then
        verify(behavior1).delete(activity1);
        verify(behavior2).delete(activity2);
        verify(taskExecutor, times(2)).runAsync(any(Runnable.class));
    }

}
