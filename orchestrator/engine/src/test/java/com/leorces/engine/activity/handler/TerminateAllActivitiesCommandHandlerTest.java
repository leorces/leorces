package com.leorces.engine.activity.handler;

import com.leorces.engine.activity.behaviour.ActivityBehavior;
import com.leorces.engine.activity.behaviour.ActivityBehaviorResolver;
import com.leorces.engine.activity.command.TerminateAllActivitiesCommand;
import com.leorces.engine.core.CommandHandler;
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

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TerminateAllActivitiesCommandHandler Tests")
class TerminateAllActivitiesCommandHandlerTest {

    @Mock
    private ActivityBehaviorResolver behaviorResolver;

    @Mock
    private TaskExecutorService taskExecutor;

    @Mock
    private ActivityBehavior activityBehavior;

    @Mock
    private ActivityExecution activity1;

    @Mock
    private ActivityExecution activity2;

    @InjectMocks
    private TerminateAllActivitiesCommandHandler handler;

    @BeforeEach
    void setUp() {
        lenient().when(activity1.id()).thenReturn("act-1");
        lenient().when(activity1.type()).thenReturn(ActivityType.EXTERNAL_TASK);
        lenient().when(activity2.id()).thenReturn("act-2");
        lenient().when(activity2.type()).thenReturn(ActivityType.RECEIVE_TASK);

        lenient().when(behaviorResolver.resolveBehavior(ActivityType.EXTERNAL_TASK)).thenReturn(activityBehavior);
        lenient().when(behaviorResolver.resolveBehavior(ActivityType.RECEIVE_TASK)).thenReturn(activityBehavior);

        lenient().when(taskExecutor.submit(any())).thenAnswer(inv -> {
            Runnable runnable = inv.getArgument(0);
            runnable.run();
            return CompletableFuture.completedFuture(null);
        });
    }

    @Test
    @DisplayName("Should return correct command type")
    void shouldReturnCorrectCommandType() {
        CommandHandler<TerminateAllActivitiesCommand> ch = handler;
        assertThat(ch.getCommandType()).isEqualTo(TerminateAllActivitiesCommand.class);
    }

    @Test
    @DisplayName("Should terminate all activities asynchronously")
    void shouldTerminateAllActivities() {
        var command = TerminateAllActivitiesCommand.of(List.of(activity1, activity2));

        handler.handle(command);

        verify(taskExecutor, times(2)).submit(any());
        verify(activityBehavior).terminate(activity1);
        verify(activityBehavior).terminate(activity2);
    }

    @Test
    @DisplayName("Should handle empty activities list")
    void shouldHandleEmptyActivities() {
        var command = TerminateAllActivitiesCommand.of(List.of());

        handler.handle(command);

        verifyNoInteractions(taskExecutor, behaviorResolver, activityBehavior);
    }

}
