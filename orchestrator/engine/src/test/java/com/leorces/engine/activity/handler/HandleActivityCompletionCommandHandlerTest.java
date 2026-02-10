package com.leorces.engine.activity.handler;

import com.leorces.api.exception.ExecutionException;
import com.leorces.engine.activity.command.CompleteActivityCommand;
import com.leorces.engine.activity.command.FindActivityCommand;
import com.leorces.engine.activity.command.HandleActivityCompletionCommand;
import com.leorces.engine.core.CommandDispatcher;
import com.leorces.engine.process.command.CompleteProcessCommand;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("HandleActivityCompletionCommandHandler Tests")
class HandleActivityCompletionCommandHandlerTest {

    @Mock
    private CommandDispatcher dispatcher;

    @Mock
    private ActivityExecution activity;

    @Mock
    private ActivityExecution parentActivity;

    @Mock
    private Process process;

    @InjectMocks
    private HandleActivityCompletionCommandHandler handler;

    @BeforeEach
    void setup() {
        when(activity.processId()).thenReturn("process-id");
        when(activity.process()).thenReturn(process);
        when(parentActivity.processId()).thenReturn("process-id");
        when(parentActivity.process()).thenReturn(process);
    }

    @Test
    @DisplayName("Complete process when activity has no parent")
    void shouldCompleteProcessWhenNoParent() {
        when(activity.hasParent()).thenReturn(false);
        var command = HandleActivityCompletionCommand.of(activity);

        handler.handle(command);

        verify(dispatcher).dispatch(CompleteProcessCommand.of("process-id"));
        verifyNoMoreInteractions(dispatcher);
    }

    @Test
    @DisplayName("Complete regular parent activity")
    void shouldCompleteRegularParentActivity() {
        when(activity.hasParent()).thenReturn(true);
        when(activity.parentDefinitionId()).thenReturn("parent-def-id");
        when(dispatcher.execute(FindActivityCommand.byDefinitionId("process-id", "parent-def-id")))
                .thenReturn(parentActivity);
        when(parentActivity.type()).thenReturn(ActivityType.SUBPROCESS);

        var command = HandleActivityCompletionCommand.of(activity);

        handler.handle(command);

        verify(dispatcher).execute(FindActivityCommand.byDefinitionId("process-id", "parent-def-id"));
        verify(dispatcher).dispatch(CompleteActivityCommand.of(parentActivity));
        verifyNoMoreInteractions(dispatcher);
    }

    @Test
    @DisplayName("Complete event subprocess without parent")
    void shouldCompleteEventSubprocessWithoutParent() {
        when(activity.hasParent()).thenReturn(true);
        when(activity.parentDefinitionId()).thenReturn("parent-def-id");
        when(dispatcher.execute(FindActivityCommand.byDefinitionId("process-id", "parent-def-id")))
                .thenReturn(parentActivity);
        when(parentActivity.type()).thenReturn(ActivityType.EVENT_SUBPROCESS);
        when(parentActivity.hasParent()).thenReturn(false);
        when(process.id()).thenReturn("process-id");

        var command = HandleActivityCompletionCommand.of(activity);

        handler.handle(command);

        verify(dispatcher).execute(FindActivityCommand.byDefinitionId("process-id", "parent-def-id"));
        verify(dispatcher).dispatch(CompleteActivityCommand.of(parentActivity));
        verifyNoMoreInteractions(dispatcher);
    }

    @Test
    @DisplayName("Complete event subprocess with parent")
    void shouldCompleteEventSubprocessWithParent() {
        when(activity.hasParent()).thenReturn(true);
        when(activity.parentDefinitionId()).thenReturn("parent-def-id");
        when(dispatcher.execute(FindActivityCommand.byDefinitionId("process-id", "parent-def-id")))
                .thenReturn(parentActivity);
        when(parentActivity.type()).thenReturn(ActivityType.EVENT_SUBPROCESS);
        when(parentActivity.hasParent()).thenReturn(true);
        when(parentActivity.parentDefinitionId()).thenReturn("parent-grandparent");

        var command = HandleActivityCompletionCommand.of(activity);

        handler.handle(command);

        verify(dispatcher).execute(FindActivityCommand.byDefinitionId("process-id", "parent-def-id"));
        verify(dispatcher).dispatch(CompleteActivityCommand.of(parentActivity));
        verifyNoMoreInteractions(dispatcher);
    }

    @Test
    @DisplayName("Throw exception when parent not found")
    void shouldThrowWhenParentNotFound() {
        when(activity.hasParent()).thenReturn(true);
        when(activity.parentDefinitionId()).thenReturn("missing-def-id");
        when(dispatcher.execute(FindActivityCommand.byDefinitionId("process-id", "missing-def-id")))
                .thenThrow(ExecutionException.of("Parent activity not found", "error"));

        var command = HandleActivityCompletionCommand.of(activity);

        assertThatThrownBy(() -> handler.handle(command))
                .isInstanceOf(ExecutionException.class)
                .hasMessage("Parent activity not found");

        verify(dispatcher).execute(FindActivityCommand.byDefinitionId("process-id", "missing-def-id"));
        verify(dispatcher, never()).dispatch(any());
    }

    @Test
    @DisplayName("Return correct command type")
    void shouldReturnCorrectCommandType() {
        assertThat(handler.getCommandType())
                .isEqualTo(HandleActivityCompletionCommand.class);
    }

}
