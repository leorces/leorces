package com.leorces.engine.activity.handler;

import com.leorces.engine.activity.behaviour.ActivityBehavior;
import com.leorces.engine.activity.behaviour.ActivityBehaviorResolver;
import com.leorces.engine.activity.command.RetryAllActivitiesCommand;
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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("RetryAllActivitiesCommandHandler Tests")
class RetryAllActivitiesCommandHandlerTest {

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
    private RetryAllActivitiesCommandHandler handler;

    @BeforeEach
    void setUp() {
        when(activityExecution1.id()).thenReturn(ACTIVITY_ID_1);
        when(activityExecution1.type()).thenReturn(ActivityType.EXTERNAL_TASK);
        when(activityExecution2.id()).thenReturn(ACTIVITY_ID_2);
        when(activityExecution2.type()).thenReturn(ActivityType.RECEIVE_TASK);

        when(behaviorResolver.resolveBehavior(any())).thenReturn(activityBehavior);
        when(taskExecutor.runAsync(any(Runnable.class)))
                .thenAnswer(invocation -> {
                    Runnable r = invocation.getArgument(0);
                    r.run();
                    return CompletableFuture.completedFuture(null);
                });
    }

    @Test
    @DisplayName("Should return correct command type")
    void shouldReturnCorrectCommandType() {
        assertThat(handler.getCommandType()).isEqualTo(RetryAllActivitiesCommand.class);
    }

    @Test
    @DisplayName("Should handle empty activities list")
    void shouldHandleEmptyActivitiesList() {
        var command = RetryAllActivitiesCommand.of(List.of());

        handler.handle(command);

        verifyNoInteractions(behaviorResolver, activityBehavior, taskExecutor);
    }

    @Test
    @DisplayName("Should handle single activity")
    void shouldHandleSingleActivity() {
        var command = RetryAllActivitiesCommand.of(List.of(activityExecution1));

        handler.handle(command);

        verify(taskExecutor).runAsync(any(Runnable.class));
        verify(activityBehavior).retry(activityExecution1);
    }

    @Test
    @DisplayName("Should handle multiple activities")
    void shouldHandleMultipleActivities() {
        var command = RetryAllActivitiesCommand.of(List.of(activityExecution1, activityExecution2));

        handler.handle(command);

        verify(taskExecutor, times(2)).runAsync(any(Runnable.class));
        verify(activityBehavior).retry(activityExecution1);
        verify(activityBehavior).retry(activityExecution2);
    }

    @Test
    @DisplayName("Should throw exception during retry")
    void shouldThrowExceptionDuringRetry() {
        doThrow(new RuntimeException("retry failed"))
                .when(activityBehavior)
                .retry(activityExecution1);

        var command = RetryAllActivitiesCommand.of(List.of(activityExecution1));

        var ex = assertThrows(RuntimeException.class, () -> handler.handle(command));

        assertThat(ex).hasMessage("retry failed");
        verify(activityBehavior).retry(activityExecution1);
    }

}
