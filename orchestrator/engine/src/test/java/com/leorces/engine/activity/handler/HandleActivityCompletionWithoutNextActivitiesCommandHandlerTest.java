package com.leorces.engine.activity.handler;

import com.leorces.engine.activity.command.CompleteActivityCommand;
import com.leorces.engine.activity.command.HandleActivityCompletionWithoutNextActivitiesCommand;
import com.leorces.engine.core.CommandDispatcher;
import com.leorces.engine.exception.activity.ActivityNotFoundException;
import com.leorces.engine.process.command.CompleteProcessCommand;
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
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
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
    @DisplayName("Should complete process when activity has no parent")
    void shouldCompleteProcessWhenNoParent() {
        // given
        when(activity.hasParent()).thenReturn(false);
        var command = HandleActivityCompletionWithoutNextActivitiesCommand.of(activity);

        // when
        handler.handle(command);

        // then
        verify(dispatcher).dispatch(CompleteProcessCommand.of("process-id"));
        verifyNoMoreInteractions(dispatcher);
    }

    @Test
    @DisplayName("Should complete parent activity when activity has parent")
    void shouldCompleteParentActivityWhenHasParent() {
        // given
        when(activity.hasParent()).thenReturn(true);
        when(activity.parentDefinitionId()).thenReturn("parent-def-id");
        when(activityPersistence.findByDefinitionId("process-id", "parent-def-id"))
                .thenReturn(Optional.of(parentActivity));
        var command = HandleActivityCompletionWithoutNextActivitiesCommand.of(activity);

        // when
        handler.handle(command);

        // then
        verify(activityPersistence).findByDefinitionId("process-id", "parent-def-id");
        verify(dispatcher).dispatchAsync(CompleteActivityCommand.of(parentActivity));
        verifyNoMoreInteractions(dispatcher);
    }

    @Test
    @DisplayName("Should throw ActivityNotFoundException when parent not found")
    void shouldThrowWhenParentNotFound() {
        // given
        when(activity.hasParent()).thenReturn(true);
        when(activity.parentDefinitionId()).thenReturn("missing-def-id");
        when(activityPersistence.findByDefinitionId("process-id", "missing-def-id"))
                .thenReturn(Optional.empty());
        var command = HandleActivityCompletionWithoutNextActivitiesCommand.of(activity);

        // expect
        assertThatThrownBy(() -> handler.handle(command))
                .isInstanceOf(ActivityNotFoundException.class)
                .hasMessageContaining("missing-def-id");

        // and no commands dispatched
        verify(dispatcher, never()).dispatch(any());
        verify(dispatcher, never()).dispatchAsync(any());
    }

    @Test
    @DisplayName("Should return correct command type")
    void shouldReturnCorrectCommandType() {
        // expect
        assertThat(handler.getCommandType())
                .isEqualTo(HandleActivityCompletionWithoutNextActivitiesCommand.class);
    }

}
