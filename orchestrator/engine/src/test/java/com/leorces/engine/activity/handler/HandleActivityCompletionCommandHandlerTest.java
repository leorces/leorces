package com.leorces.engine.activity.handler;

import com.leorces.engine.activity.behaviour.ActivityCompletionResult;
import com.leorces.engine.activity.command.HandleActivityCompletionCommand;
import com.leorces.engine.activity.command.HandleActivityCompletionWithNextActivitiesCommand;
import com.leorces.engine.activity.command.HandleActivityCompletionWithoutNextActivitiesCommand;
import com.leorces.engine.core.CommandDispatcher;
import com.leorces.model.definition.activity.ActivityDefinition;
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

import java.util.List;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("HandleActivityCompletionCommandHandler tests")
class HandleActivityCompletionCommandHandlerTest {

    @Mock
    private CommandDispatcher dispatcher;

    @Mock
    private ActivityExecution activityExecution;

    @Mock
    private ActivityDefinition nextActivity;

    @Mock
    private Process process;

    @InjectMocks
    private HandleActivityCompletionCommandHandler handler;

    @BeforeEach
    void setUp() {
        when(activityExecution.process()).thenReturn(process);
    }

    @Test
    @DisplayName("Should dispatch HandleActivityCompletionWithoutNextActivitiesCommand when there are no next activities")
    void shouldDispatchWithoutNextActivitiesCommand() {
        // Given
        var result = ActivityCompletionResult.completed(activityExecution, List.of());
        var command = HandleActivityCompletionCommand.of(result);

        // When
        handler.handle(command);

        // Then
        verify(dispatcher).dispatch(HandleActivityCompletionWithoutNextActivitiesCommand.of(activityExecution));
        verify(dispatcher, never()).dispatch(any(HandleActivityCompletionWithNextActivitiesCommand.class));
    }

    @Test
    @DisplayName("Should dispatch HandleActivityCompletionWithNextActivitiesCommand when next activities exist")
    void shouldDispatchWithNextActivitiesCommand() {
        // Given
        var nextActivities = List.of(nextActivity);
        var result = ActivityCompletionResult.completed(activityExecution, nextActivities);
        var command = HandleActivityCompletionCommand.of(result);

        // When
        handler.handle(command);

        // Then
        verify(dispatcher).dispatch(HandleActivityCompletionWithNextActivitiesCommand.of(process, nextActivities));
        verify(dispatcher, never()).dispatch(any(HandleActivityCompletionWithoutNextActivitiesCommand.class));
    }

    @Test
    @DisplayName("Should not dispatch HandleActivityCompletionWithNextActivitiesCommand when next activities list is empty")
    void shouldNotDispatchWithNextActivitiesWhenEmpty() {
        // Given
        var result = ActivityCompletionResult.completed(activityExecution, List.of());
        var command = HandleActivityCompletionCommand.of(result);

        // When
        handler.handle(command);

        // Then
        verify(dispatcher, never()).dispatch(any(HandleActivityCompletionWithNextActivitiesCommand.class));
        verify(dispatcher).dispatch(HandleActivityCompletionWithoutNextActivitiesCommand.of(activityExecution));
    }

}
