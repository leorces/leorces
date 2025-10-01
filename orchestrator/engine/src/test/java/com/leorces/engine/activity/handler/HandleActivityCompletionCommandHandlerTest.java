package com.leorces.engine.activity.handler;

import com.leorces.engine.activity.behaviour.ActivityBehavior;
import com.leorces.engine.activity.behaviour.ActivityBehaviorResolver;
import com.leorces.engine.activity.command.CompleteActivityCommand;
import com.leorces.engine.activity.command.HandleActivityCompletionCommand;
import com.leorces.engine.activity.command.RunActivityCommand;
import com.leorces.engine.activity.command.TerminateActivityCommand;
import com.leorces.engine.core.CommandDispatcher;
import com.leorces.engine.exception.activity.ActivityNotFoundException;
import com.leorces.engine.process.command.CompleteProcessCommand;
import com.leorces.engine.process.command.ResolveProcessIncidentCommand;
import com.leorces.engine.process.command.TerminateProcessCommand;
import com.leorces.model.definition.activity.ActivityDefinition;
import com.leorces.model.definition.activity.ActivityType;
import com.leorces.model.runtime.activity.ActivityExecution;
import com.leorces.model.runtime.process.Process;
import com.leorces.model.runtime.process.ProcessState;
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

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("HandleActivityCompletionCommandHandler Tests")
class HandleActivityCompletionCommandHandlerTest {

    private static final String ACTIVITY_ID = "activity-id";
    private static final String PROCESS_ID = "process-id";
    private static final String DEFINITION_ID = "definition-id";
    private static final String PARENT_DEFINITION_ID = "parent-definition-id";

    @Mock
    private ActivityPersistence activityPersistence;

    @Mock
    private ActivityBehaviorResolver behaviorResolver;

    @Mock
    private CommandDispatcher dispatcher;

    @Mock
    private ActivityBehavior activityBehavior;

    @Mock
    private ActivityExecution activityExecution;

    @Mock
    private ActivityExecution parentActivity;

    @Mock
    private Process process;

    @Mock
    private ActivityDefinition nextActivityDefinition;

    @InjectMocks
    private HandleActivityCompletionCommandHandler handler;

    @BeforeEach
    void setUp() {
        when(activityExecution.id()).thenReturn(ACTIVITY_ID);
        when(activityExecution.processId()).thenReturn(PROCESS_ID);
        when(activityExecution.definitionId()).thenReturn(DEFINITION_ID);
        when(activityExecution.type()).thenReturn(ActivityType.EXTERNAL_TASK);
        when(activityExecution.process()).thenReturn(process);
        when(activityExecution.parentDefinitionId()).thenReturn(null);
        when(activityExecution.isAsync()).thenReturn(false);
        when(process.state()).thenReturn(ProcessState.ACTIVE);
        when(process.id()).thenReturn(PROCESS_ID);
        when(behaviorResolver.resolveBehavior(ActivityType.EXTERNAL_TASK)).thenReturn(activityBehavior);
    }

    @Test
    @DisplayName("Should return correct command type")
    void shouldReturnCorrectCommandType() {
        // Given & When
        var commandType = handler.getCommandType();

        // Then
        assertThat(commandType).isEqualTo(HandleActivityCompletionCommand.class);
    }

    @Test
    @DisplayName("Should complete process when no next activities and no parent activity")
    void shouldCompleteProcessWhenNoNextActivitiesAndNoParentActivity() {
        // Given
        when(activityBehavior.getNextActivities(activityExecution)).thenReturn(Collections.emptyList());
        var command = HandleActivityCompletionCommand.of(activityExecution);

        // When
        handler.handle(command);

        // Then
        verify(behaviorResolver).resolveBehavior(ActivityType.EXTERNAL_TASK);
        verify(activityBehavior).getNextActivities(activityExecution);
        verify(dispatcher).dispatchAsync(any(CompleteProcessCommand.class));
        verify(dispatcher, never()).dispatchAsync(any(CompleteActivityCommand.class));
    }

    @Test
    @DisplayName("Should complete parent activity when no next activities and has parent activity")
    void shouldCompleteParentActivityWhenNoNextActivitiesAndHasParentActivity() {
        // Given
        when(activityExecution.parentDefinitionId()).thenReturn(PARENT_DEFINITION_ID);
        when(activityBehavior.getNextActivities(activityExecution)).thenReturn(Collections.emptyList());
        when(activityPersistence.findByDefinitionId(PROCESS_ID, PARENT_DEFINITION_ID)).thenReturn(Optional.of(parentActivity));
        var command = HandleActivityCompletionCommand.of(activityExecution);

        // When
        handler.handle(command);

        // Then
        verify(activityPersistence).findByDefinitionId(PROCESS_ID, PARENT_DEFINITION_ID);
        verify(dispatcher).dispatchAsync(any(CompleteActivityCommand.class));
        verify(dispatcher, never()).dispatchAsync(any(CompleteProcessCommand.class));
    }

    @Test
    @DisplayName("Should not complete process when activity is async and no parent activity")
    void shouldNotCompleteProcessWhenActivityIsAsyncAndNoParentActivity() {
        // Given
        when(activityExecution.isAsync()).thenReturn(true);
        when(activityBehavior.getNextActivities(activityExecution)).thenReturn(Collections.emptyList());
        var command = HandleActivityCompletionCommand.of(activityExecution);

        // When
        handler.handle(command);

        // Then
        verify(dispatcher, never()).dispatchAsync(any(CompleteProcessCommand.class));
        verify(dispatcher, never()).dispatchAsync(any(CompleteActivityCommand.class));
    }

    @Test
    @DisplayName("Should run next activities when they exist")
    void shouldRunNextActivitiesWhenTheyExist() {
        // Given
        var nextActivities = List.of(nextActivityDefinition);
        when(activityBehavior.getNextActivities(activityExecution)).thenReturn(nextActivities);
        var command = HandleActivityCompletionCommand.of(activityExecution);

        // When
        handler.handle(command);

        // Then
        verify(dispatcher).dispatchAsync(any(RunActivityCommand.class));
        verify(dispatcher, never()).dispatchAsync(any(CompleteProcessCommand.class));
        verify(dispatcher, never()).dispatchAsync(any(CompleteActivityCommand.class));
    }

    @Test
    @DisplayName("Should resolve process incident when process is in incident state and has next activities")
    void shouldResolveProcessIncidentWhenProcessInIncidentStateAndHasNextActivities() {
        // Given
        when(process.state()).thenReturn(ProcessState.INCIDENT);
        var nextActivities = List.of(nextActivityDefinition);
        when(activityBehavior.getNextActivities(activityExecution)).thenReturn(nextActivities);
        var command = HandleActivityCompletionCommand.of(activityExecution);

        // When
        handler.handle(command);

        // Then
        verify(dispatcher).dispatch(any(ResolveProcessIncidentCommand.class));
        verify(dispatcher).dispatchAsync(any(RunActivityCommand.class));
    }

    @Test
    @DisplayName("Should handle error end event without dispatching commands")
    void shouldHandleErrorEndEventWithoutDispatchingCommands() {
        // Given
        when(activityExecution.type()).thenReturn(ActivityType.ERROR_END_EVENT);
        when(behaviorResolver.resolveBehavior(ActivityType.ERROR_END_EVENT)).thenReturn(activityBehavior);
        when(activityBehavior.getNextActivities(activityExecution)).thenReturn(Collections.emptyList());
        var command = HandleActivityCompletionCommand.of(activityExecution);

        // When
        handler.handle(command);

        // Then
        verify(dispatcher, never()).dispatch(any());
        verify(dispatcher, never()).dispatchAsync(any());
    }

    @Test
    @DisplayName("Should terminate process when terminate end event without parent activity")
    void shouldTerminateProcessWhenTerminateEndEventWithoutParentActivity() {
        // Given
        when(activityExecution.type()).thenReturn(ActivityType.TERMINATE_END_EVENT);
        when(behaviorResolver.resolveBehavior(ActivityType.TERMINATE_END_EVENT)).thenReturn(activityBehavior);
        when(activityBehavior.getNextActivities(activityExecution)).thenReturn(Collections.emptyList());
        var command = HandleActivityCompletionCommand.of(activityExecution);

        // When
        handler.handle(command);

        // Then
        verify(dispatcher).dispatch(any(TerminateProcessCommand.class));
    }

    @Test
    @DisplayName("Should terminate parent activity and process when terminate end event with parent activity")
    void shouldTerminateParentActivityAndProcessWhenTerminateEndEventWithParentActivity() {
        // Given
        when(activityExecution.type()).thenReturn(ActivityType.TERMINATE_END_EVENT);
        when(activityExecution.parentDefinitionId()).thenReturn(PARENT_DEFINITION_ID);
        when(behaviorResolver.resolveBehavior(ActivityType.TERMINATE_END_EVENT)).thenReturn(activityBehavior);
        when(activityBehavior.getNextActivities(activityExecution)).thenReturn(Collections.emptyList());
        when(activityPersistence.findByDefinitionId(PROCESS_ID, PARENT_DEFINITION_ID)).thenReturn(Optional.of(parentActivity));
        when(parentActivity.id()).thenReturn("parent-activity-id");
        var command = HandleActivityCompletionCommand.of(activityExecution);

        // When
        handler.handle(command);

        // Then
        verify(dispatcher).dispatch(any(TerminateActivityCommand.class));
        verify(dispatcher).dispatch(any(TerminateProcessCommand.class));
    }

    @Test
    @DisplayName("Should throw exception when parent activity not found")
    void shouldThrowExceptionWhenParentActivityNotFound() {
        // Given
        when(activityExecution.parentDefinitionId()).thenReturn(PARENT_DEFINITION_ID);
        when(activityBehavior.getNextActivities(activityExecution)).thenReturn(Collections.emptyList());
        when(activityPersistence.findByDefinitionId(PROCESS_ID, PARENT_DEFINITION_ID)).thenReturn(Optional.empty());
        var command = HandleActivityCompletionCommand.of(activityExecution);

        // When & Then
        assertThatThrownBy(() -> handler.handle(command))
                .isInstanceOf(ActivityNotFoundException.class);
    }

    @Test
    @DisplayName("Should handle multiple next activities")
    void shouldHandleMultipleNextActivities() {
        // Given
        var nextActivity1 = mock(ActivityDefinition.class);
        var nextActivity2 = mock(ActivityDefinition.class);
        var nextActivities = List.of(nextActivity1, nextActivity2);
        when(activityBehavior.getNextActivities(activityExecution)).thenReturn(nextActivities);
        var command = HandleActivityCompletionCommand.of(activityExecution);

        // When
        handler.handle(command);

        // Then
        verify(dispatcher, times(2)).dispatchAsync(any(RunActivityCommand.class));
    }

    @Test
    @DisplayName("Should handle different activity types")
    void shouldHandleDifferentActivityTypes() {
        // Given
        when(activityExecution.type()).thenReturn(ActivityType.RECEIVE_TASK);
        when(behaviorResolver.resolveBehavior(ActivityType.RECEIVE_TASK)).thenReturn(activityBehavior);
        when(activityBehavior.getNextActivities(activityExecution)).thenReturn(Collections.emptyList());
        var command = HandleActivityCompletionCommand.of(activityExecution);

        // When
        handler.handle(command);

        // Then
        verify(behaviorResolver).resolveBehavior(ActivityType.RECEIVE_TASK);
        verify(activityBehavior).getNextActivities(activityExecution);
        verify(dispatcher).dispatchAsync(any(CompleteProcessCommand.class));
    }

}