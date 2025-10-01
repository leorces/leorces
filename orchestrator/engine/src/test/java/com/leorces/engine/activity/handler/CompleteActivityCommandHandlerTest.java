package com.leorces.engine.activity.handler;

import com.leorces.engine.activity.ActivityFactory;
import com.leorces.engine.activity.behaviour.ActivityBehavior;
import com.leorces.engine.activity.behaviour.ActivityBehaviorResolver;
import com.leorces.engine.activity.command.CompleteActivityCommand;
import com.leorces.engine.activity.command.FailActivityCommand;
import com.leorces.engine.activity.command.HandleActivityCompletionCommand;
import com.leorces.engine.core.CommandDispatcher;
import com.leorces.engine.exception.ExecutionException;
import com.leorces.engine.variables.VariablesService;
import com.leorces.engine.variables.command.SetVariablesCommand;
import com.leorces.model.definition.activity.ActivityType;
import com.leorces.model.runtime.activity.ActivityExecution;
import com.leorces.model.runtime.activity.ActivityState;
import com.leorces.model.runtime.process.Process;
import com.leorces.model.runtime.variable.Variable;
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
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("CompleteActivityCommandHandler Tests")
class CompleteActivityCommandHandlerTest {

    private static final String ACTIVITY_ID = "activity-id";
    private static final String PROCESS_ID = "process-id";
    private static final String DEFINITION_ID = "definition-id";
    private static final Map<String, Object> INPUT_VARIABLES = Map.of("inputVar", "inputValue");
    private static final Map<String, Object> OUTPUT_VARIABLES = Map.of("outputVar", "outputValue");
    private static final Map<String, Object> OUTPUT_MAPPINGS = Map.of("outputMapping", "value");

    @Mock
    private ActivityBehaviorResolver behaviorResolver;

    @Mock
    private VariablesService variablesService;

    @Mock
    private ActivityFactory activityFactory;

    @Mock
    private CommandDispatcher dispatcher;

    @Mock
    private ActivityBehavior activityBehavior;

    @Mock
    private ActivityExecution activityExecution;

    @Mock
    private Process process;

    @Mock
    private Variable outputVariable;

    @InjectMocks
    private CompleteActivityCommandHandler handler;

    @BeforeEach
    void setUp() {
        when(activityExecution.id()).thenReturn(ACTIVITY_ID);
        when(activityExecution.processId()).thenReturn(PROCESS_ID);
        when(activityExecution.definitionId()).thenReturn(DEFINITION_ID);
        when(activityExecution.type()).thenReturn(ActivityType.EXTERNAL_TASK);
        when(activityExecution.state()).thenReturn(ActivityState.ACTIVE);
        when(activityExecution.isInTerminalState()).thenReturn(false);
        when(activityExecution.process()).thenReturn(process);
        when(activityExecution.outputs()).thenReturn(OUTPUT_MAPPINGS);
        when(process.isInTerminalState()).thenReturn(false);
        when(behaviorResolver.resolveBehavior(ActivityType.EXTERNAL_TASK)).thenReturn(activityBehavior);
        when(activityBehavior.complete(activityExecution)).thenReturn(activityExecution);
        when(variablesService.evaluate(activityExecution, OUTPUT_MAPPINGS)).thenReturn(List.of(outputVariable));
        when(variablesService.toMap(List.of(outputVariable))).thenReturn(OUTPUT_VARIABLES);
    }

    @Test
    @DisplayName("Should return correct command type")
    void shouldReturnCorrectCommandType() {
        // Given & When
        var commandType = handler.getCommandType();

        // Then
        assertThat(commandType).isEqualTo(CompleteActivityCommand.class);
    }

    @Test
    @DisplayName("Should complete activity with provided activity and variables")
    void shouldCompleteActivityWithProvidedActivityAndVariables() {
        // Given
        var command = CompleteActivityCommand.of(activityExecution, INPUT_VARIABLES);

        // When
        handler.handle(command);

        // Then
        verify(behaviorResolver).resolveBehavior(ActivityType.EXTERNAL_TASK);
        verify(activityBehavior).complete(activityExecution);
        verify(variablesService).evaluate(activityExecution, OUTPUT_MAPPINGS);
        verify(variablesService).toMap(List.of(outputVariable));
        verify(dispatcher).dispatch(any(SetVariablesCommand.class));
        verify(dispatcher).dispatchAsync(any(HandleActivityCompletionCommand.class));
    }

    @Test
    @DisplayName("Should complete activity with activity ID")
    void shouldCompleteActivityWithActivityId() {
        // Given
        var command = CompleteActivityCommand.of(ACTIVITY_ID, INPUT_VARIABLES);
        when(activityFactory.getById(ACTIVITY_ID)).thenReturn(activityExecution);

        // When
        handler.handle(command);

        // Then
        verify(activityFactory).getById(ACTIVITY_ID);
        verify(behaviorResolver).resolveBehavior(ActivityType.EXTERNAL_TASK);
        verify(activityBehavior).complete(activityExecution);
        verify(dispatcher).dispatch(any(SetVariablesCommand.class));
        verify(dispatcher).dispatchAsync(any(HandleActivityCompletionCommand.class));
    }

    @Test
    @DisplayName("Should combine input and output variables correctly")
    void shouldCombineInputAndOutputVariablesCorrectly() {
        // Given
        var command = CompleteActivityCommand.of(activityExecution, INPUT_VARIABLES);

        // When
        handler.handle(command);

        // Then
        verify(dispatcher).dispatch(argThat(cmd -> {
            if (cmd instanceof SetVariablesCommand setVarCmd) {
                var variables = setVarCmd.variables();
                return variables.containsKey("inputVar") &&
                        variables.containsKey("outputVar") &&
                        "inputValue".equals(variables.get("inputVar")) &&
                        "outputValue".equals(variables.get("outputVar"));
            }
            return false;
        }));
    }

    @Test
    @DisplayName("Should handle empty variables")
    void shouldHandleEmptyVariables() {
        // Given
        var command = CompleteActivityCommand.of(activityExecution, Collections.emptyMap());
        when(variablesService.toMap(List.of(outputVariable))).thenReturn(Collections.emptyMap());

        // When
        handler.handle(command);

        // Then
        verify(dispatcher).dispatch(any(SetVariablesCommand.class));
        verify(dispatcher).dispatchAsync(any(HandleActivityCompletionCommand.class));
    }

    @Test
    @DisplayName("Should complete activity when in terminal state but process is not in terminal state")
    void shouldCompleteActivityWhenInTerminalStateButProcessNotInTerminalState() {
        // Given
        when(activityExecution.state()).thenReturn(ActivityState.COMPLETED);
        when(activityExecution.isInTerminalState()).thenReturn(true);
        // process.isInTerminalState() returns false by default, so canHandle() will return true
        var command = CompleteActivityCommand.of(activityExecution, INPUT_VARIABLES);

        // When
        handler.handle(command);

        // Then
        verify(behaviorResolver).resolveBehavior(ActivityType.EXTERNAL_TASK);
        verify(activityBehavior).complete(activityExecution);
        verify(dispatcher).dispatch(any(SetVariablesCommand.class));
        verify(dispatcher).dispatchAsync(any(HandleActivityCompletionCommand.class));
    }

    @Test
    @DisplayName("Should not complete activity when both activity and process are in terminal state")
    void shouldNotCompleteActivityWhenBothActivityAndProcessInTerminalState() {
        // Given
        when(activityExecution.state()).thenReturn(ActivityState.COMPLETED);
        when(activityExecution.isInTerminalState()).thenReturn(true);
        when(process.isInTerminalState()).thenReturn(true);
        // All conditions in canHandle() OR logic are false: state != null && isInTerminalState && process.isInTerminalState
        var command = CompleteActivityCommand.of(activityExecution, INPUT_VARIABLES);

        // When
        handler.handle(command);

        // Then
        verify(behaviorResolver, never()).resolveBehavior(any());
        verify(activityBehavior, never()).complete(any());
        verify(dispatcher, never()).dispatch(any());
        verify(dispatcher, never()).dispatchAsync(any());
    }

    @Test
    @DisplayName("Should dispatch fail command and throw exception when completion fails")
    void shouldDispatchFailCommandAndThrowExceptionWhenCompletionFails() {
        // Given
        var command = CompleteActivityCommand.of(activityExecution, INPUT_VARIABLES);
        var exception = new RuntimeException("Completion failed");
        when(activityBehavior.complete(activityExecution)).thenThrow(exception);

        // When & Then
        assertThatThrownBy(() -> handler.handle(command))
                .isInstanceOf(ExecutionException.class)
                .hasMessageContaining("Activity completion failed")
                .hasCause(exception);

        verify(dispatcher).dispatch(any(FailActivityCommand.class));
    }

    @Test
    @DisplayName("Should handle different activity types")
    void shouldHandleDifferentActivityTypes() {
        // Given
        when(activityExecution.type()).thenReturn(ActivityType.RECEIVE_TASK);
        when(behaviorResolver.resolveBehavior(ActivityType.RECEIVE_TASK)).thenReturn(activityBehavior);
        var command = CompleteActivityCommand.of(activityExecution, INPUT_VARIABLES);

        // When
        handler.handle(command);

        // Then
        verify(behaviorResolver).resolveBehavior(ActivityType.RECEIVE_TASK);
        verify(activityBehavior).complete(activityExecution);
        verify(dispatcher).dispatchAsync(any(HandleActivityCompletionCommand.class));
    }

    @Test
    @DisplayName("Should handle activity with null state")
    void shouldHandleActivityWithNullState() {
        // Given
        when(activityExecution.state()).thenReturn(null);
        var command = CompleteActivityCommand.of(activityExecution, INPUT_VARIABLES);

        // When
        handler.handle(command);

        // Then
        verify(behaviorResolver).resolveBehavior(ActivityType.EXTERNAL_TASK);
        verify(activityBehavior).complete(activityExecution);
        verify(dispatcher).dispatchAsync(any(HandleActivityCompletionCommand.class));
    }

}