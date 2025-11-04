package com.leorces.engine.activity.handler;

import com.leorces.engine.activity.command.CompleteActivityCommand;
import com.leorces.engine.activity.command.HandleActivityCompletionWithoutNextActivitiesCommand;
import com.leorces.engine.activity.command.TerminateActivityCommand;
import com.leorces.engine.core.CommandDispatcher;
import com.leorces.engine.exception.activity.ActivityNotFoundException;
import com.leorces.engine.process.command.CompleteProcessCommand;
import com.leorces.engine.process.command.TerminateProcessCommand;
import com.leorces.model.definition.activity.ActivityType;
import com.leorces.model.runtime.activity.ActivityExecution;
import com.leorces.persistence.ActivityPersistence;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("HandleActivityCompletionWithoutNextActivitiesCommandHandler Tests")
class HandleActivityCompletionWithoutNextActivitiesCommandHandlerTest {

    @Mock
    private ActivityPersistence activityPersistence;

    @Mock
    private CommandDispatcher dispatcher;

    @Mock
    private ActivityExecution activity;

    @Mock
    private ActivityExecution parentActivity;

    @InjectMocks
    private HandleActivityCompletionWithoutNextActivitiesCommandHandler handler;

    @BeforeEach
    void setup() {
        when(activity.processId()).thenReturn("process-id");
    }

    @Test
    @DisplayName("Should complete process when no parent and not async")
    void shouldCompleteProcessWhenNoParent() {
        when(activity.hasParent()).thenReturn(false);
        when(activity.isAsync()).thenReturn(false);
        when(activity.type()).thenReturn(ActivityType.EXTERNAL_TASK);
        var command = HandleActivityCompletionWithoutNextActivitiesCommand.of(activity);

        handler.handle(command);

        verify(dispatcher).dispatchAsync(any(CompleteProcessCommand.class));
    }

    @Test
    @DisplayName("Should complete parent activity when has parent and not async")
    void shouldCompleteParentActivityWhenHasParent() {
        when(activity.hasParent()).thenReturn(true);
        when(activity.isAsync()).thenReturn(false);
        when(activity.type()).thenReturn(ActivityType.EXTERNAL_TASK);
        when(activity.parentDefinitionId()).thenReturn("parent-def-id");
        when(activityPersistence.findByDefinitionId("process-id", "parent-def-id")).thenReturn(Optional.of(parentActivity));
        var command = HandleActivityCompletionWithoutNextActivitiesCommand.of(activity);

        handler.handle(command);

        verify(dispatcher).dispatchAsync(CompleteActivityCommand.of(parentActivity));
    }

    @Test
    @DisplayName("Should throw exception when parent activity not found")
    void shouldThrowWhenParentNotFound() {
        when(activity.hasParent()).thenReturn(true);
        when(activity.isAsync()).thenReturn(false);
        when(activity.type()).thenReturn(ActivityType.EXTERNAL_TASK);
        when(activity.parentDefinitionId()).thenReturn("missing-id");
        when(activityPersistence.findByDefinitionId("process-id", "missing-id")).thenReturn(Optional.empty());
        var command = HandleActivityCompletionWithoutNextActivitiesCommand.of(activity);

        assertThatThrownBy(() -> handler.handle(command))
                .isInstanceOf(ActivityNotFoundException.class);
    }

    @Test
    @DisplayName("Should skip handling for ERROR_END_EVENT")
    void shouldSkipErrorEndEvent() {
        when(activity.type()).thenReturn(ActivityType.ERROR_END_EVENT);
        var command = HandleActivityCompletionWithoutNextActivitiesCommand.of(activity);

        handler.handle(command);

        verifyNoInteractions(dispatcher);
    }

    @Test
    @DisplayName("Should terminate process for TERMINATE_END_EVENT without parent")
    void shouldTerminateProcessWithoutParent() {
        when(activity.type()).thenReturn(ActivityType.TERMINATE_END_EVENT);
        when(activity.hasParent()).thenReturn(false);
        var command = HandleActivityCompletionWithoutNextActivitiesCommand.of(activity);

        handler.handle(command);

        verify(dispatcher).dispatch(any(TerminateProcessCommand.class));
    }

    @Test
    @DisplayName("Should terminate parent and process for TERMINATE_END_EVENT with parent")
    void shouldTerminateParentAndProcess() {
        when(activity.type()).thenReturn(ActivityType.TERMINATE_END_EVENT);
        when(activity.hasParent()).thenReturn(true);
        when(activity.parentDefinitionId()).thenReturn("parent-id");
        when(activityPersistence.findByDefinitionId("process-id", "parent-id")).thenReturn(Optional.of(parentActivity));
        when(parentActivity.id()).thenReturn("parent-activity-id");
        when(parentActivity.type()).thenReturn(ActivityType.SUBPROCESS);

        var command = HandleActivityCompletionWithoutNextActivitiesCommand.of(activity);

        handler.handle(command);

        verify(dispatcher).dispatch(TerminateActivityCommand.of("parent-activity-id"));
        verify(dispatcher).dispatch(TerminateProcessCommand.of("process-id"));
    }

}
