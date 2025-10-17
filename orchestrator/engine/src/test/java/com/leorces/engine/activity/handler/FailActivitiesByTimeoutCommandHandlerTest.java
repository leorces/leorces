package com.leorces.engine.activity.handler;

import com.leorces.engine.activity.command.FailActivitiesByTimeoutCommand;
import com.leorces.engine.activity.command.FailActivityCommand;
import com.leorces.engine.core.CommandDispatcher;
import com.leorces.model.runtime.activity.ActivityExecution;
import com.leorces.model.runtime.activity.ActivityFailure;
import com.leorces.persistence.ActivityPersistence;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("FailActivitiesByTimeoutCommandHandler Tests")
class FailActivitiesByTimeoutCommandHandlerTest {

    @Mock
    private ActivityPersistence activityPersistence;

    @Mock
    private CommandDispatcher dispatcher;

    @Mock
    private ActivityExecution activity1;

    @Mock
    private ActivityExecution activity2;

    @InjectMocks
    private FailActivitiesByTimeoutCommandHandler handler;

    @BeforeEach
    void setUp() {
        when(activity1.id()).thenReturn("activity-1");
        when(activity1.processId()).thenReturn("process-1");
        when(activity2.id()).thenReturn("activity-2");
        when(activity2.processId()).thenReturn("process-2");
    }

    @Test
    @DisplayName("Should return correct command type")
    void shouldReturnCorrectCommandType() {
        // When
        var type = handler.getCommandType();

        // Then
        assertThat(type).isEqualTo(FailActivitiesByTimeoutCommand.class);
    }

    @Test
    @DisplayName("Should fail all timed out activities")
    void shouldFailAllTimedOutActivities() {
        // Given
        when(activityPersistence.findTimedOut()).thenReturn(List.of(activity1, activity2));
        var command = new FailActivitiesByTimeoutCommand();

        // When
        handler.handle(command);

        // Then
        verify(activityPersistence).findTimedOut();
        verify(dispatcher, times(2)).dispatchAsync(any(FailActivityCommand.class));

        // Verify that each command contains expected activity and failure
        verify(dispatcher).dispatchAsync(argThat(cmd -> {
            if (cmd instanceof FailActivityCommand failCmd) {
                return failCmd.activity().equals(activity1)
                        && "Timeout".equals(failCmd.failure().reason());
            }
            return false;
        }));

        verify(dispatcher).dispatchAsync(argThat(cmd -> {
            if (cmd instanceof FailActivityCommand failCmd) {
                return failCmd.activity().equals(activity2)
                        && "Timeout".equals(failCmd.failure().reason());
            }
            return false;
        }));
    }

    @Test
    @DisplayName("Should handle empty timed out activity list without errors")
    void shouldHandleEmptyTimedOutActivities() {
        // Given
        when(activityPersistence.findTimedOut()).thenReturn(List.of());
        var command = new FailActivitiesByTimeoutCommand();

        // When
        handler.handle(command);

        // Then
        verify(activityPersistence).findTimedOut();
        verify(dispatcher, never()).dispatchAsync(any());
    }

    @Test
    @DisplayName("Should call failActivity() for each activity individually")
    void shouldCallFailActivityIndividually() {
        // Given
        when(activityPersistence.findTimedOut()).thenReturn(List.of(activity1, activity2));
        var command = new FailActivitiesByTimeoutCommand();

        // When
        handler.handle(command);

        // Then
        verify(dispatcher, times(2)).dispatchAsync(any(FailActivityCommand.class));

        InOrder inOrder = inOrder(dispatcher);
        inOrder.verify(dispatcher).dispatchAsync(argThat(cmd ->
                cmd instanceof FailActivityCommand failCmd &&
                        failCmd.activity().equals(activity1)
        ));
        inOrder.verify(dispatcher).dispatchAsync(argThat(cmd ->
                cmd instanceof FailActivityCommand failCmd &&
                        failCmd.activity().equals(activity2)
        ));
    }

    @Test
    @DisplayName("Should dispatch FailActivityCommand with Timeout failure reason")
    void shouldDispatchFailActivityCommandWithTimeoutFailure() {
        // Given
        when(activityPersistence.findTimedOut()).thenReturn(List.of(activity1));
        var command = new FailActivitiesByTimeoutCommand();

        // When
        handler.handle(command);

        // Then
        verify(dispatcher).dispatchAsync(argThat(cmd -> {
            if (cmd instanceof FailActivityCommand failCmd) {
                ActivityFailure failure = failCmd.failure();
                return failure.reason().equals("Timeout") && failCmd.activity().equals(activity1);
            }
            return false;
        }));
    }

}
