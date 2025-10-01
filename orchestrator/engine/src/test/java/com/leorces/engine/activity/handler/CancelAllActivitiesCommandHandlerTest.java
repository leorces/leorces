package com.leorces.engine.activity.handler;

import com.leorces.engine.activity.behaviour.ActivityBehavior;
import com.leorces.engine.activity.behaviour.ActivityBehaviorResolver;
import com.leorces.engine.activity.command.CancelAllActivitiesCommand;
import com.leorces.engine.service.TaskExecutorService;
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

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("CancelAllActivitiesCommandHandler Tests")
class CancelAllActivitiesCommandHandlerTest {

    private static final String ACTIVITY_ID_1 = "activity-id-1";
    private static final String ACTIVITY_ID_2 = "activity-id-2";

    @Mock
    private ActivityBehaviorResolver behaviorResolver;

    @Mock
    private TaskExecutorService taskExecutor;

    @Mock
    private ActivityBehavior activityBehavior;

    @Mock
    private ActivityExecution activityExecution1;

    @Mock
    private ActivityExecution activityExecution2;

    @InjectMocks
    private CancelAllActivitiesCommandHandler handler;

    @BeforeEach
    void setUp() {
        when(activityExecution1.id()).thenReturn(ACTIVITY_ID_1);
        when(activityExecution1.type()).thenReturn(ActivityType.EXTERNAL_TASK);
        when(activityExecution2.id()).thenReturn(ACTIVITY_ID_2);
        when(activityExecution2.type()).thenReturn(ActivityType.RECEIVE_TASK);
        when(behaviorResolver.resolveBehavior(any())).thenReturn(activityBehavior);
        when(taskExecutor.submit(any(Runnable.class))).thenReturn(CompletableFuture.completedFuture(null));
    }

    @Test
    @DisplayName("Should return correct command type")
    void shouldReturnCorrectCommandType() {
        // Given & When
        var commandType = handler.getCommandType();

        // Then
        assertThat(commandType).isEqualTo(CancelAllActivitiesCommand.class);
    }

    @Test
    @DisplayName("Should handle empty activities list")
    void shouldHandleEmptyActivitiesList() {
        // Given
        var activities = List.<ActivityExecution>of();
        var command = CancelAllActivitiesCommand.of(activities);

        // When
        handler.handle(command);

        // Then
        // Should complete without errors
        assertThat(command.activities()).isEmpty();
    }

    @Test
    @DisplayName("Should handle single activity")
    void shouldHandleSingleActivity() {
        // Given
        var activities = List.of(activityExecution1);
        var command = CancelAllActivitiesCommand.of(activities);

        // When
        handler.handle(command);

        // Then
        verify(taskExecutor, times(1)).submit(any(Runnable.class));
    }

    @Test
    @DisplayName("Should handle multiple activities")
    void shouldHandleMultipleActivities() {
        // Given
        var activities = List.of(activityExecution1, activityExecution2);
        var command = CancelAllActivitiesCommand.of(activities);

        // When
        handler.handle(command);

        // Then
        verify(taskExecutor, times(2)).submit(any(Runnable.class));
    }

    @Test
    @DisplayName("Should submit cancellation tasks to executor")
    void shouldSubmitCancellationTasksToExecutor() {
        // Given
        var activities = List.of(activityExecution1, activityExecution2);
        var command = CancelAllActivitiesCommand.of(activities);

        // When
        handler.handle(command);

        // Then
        verify(taskExecutor, times(2)).submit(any(Runnable.class));
    }

}