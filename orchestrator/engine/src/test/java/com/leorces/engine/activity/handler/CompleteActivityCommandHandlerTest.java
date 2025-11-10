package com.leorces.engine.activity.handler;

import com.leorces.engine.activity.ActivityFactory;
import com.leorces.engine.activity.behaviour.ActivityBehavior;
import com.leorces.engine.activity.behaviour.ActivityBehaviorResolver;
import com.leorces.engine.activity.command.CompleteActivityCommand;
import com.leorces.engine.activity.command.FailActivityCommand;
import com.leorces.engine.core.CommandDispatcher;
import com.leorces.engine.exception.ExecutionException;
import com.leorces.model.definition.activity.ActivityType;
import com.leorces.model.runtime.activity.ActivityExecution;
import com.leorces.model.runtime.activity.ActivityState;
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

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("CompleteActivityCommandHandler Tests")
class CompleteActivityCommandHandlerTest {

    private static final String ACTIVITY_ID = "activity-id";
    private static final String PROCESS_ID = "process-id";
    private static final String DEFINITION_ID = "definition-id";
    private static final Map<String, Object> VARIABLES = Map.of("var1", "val1");

    @Mock
    private ActivityBehaviorResolver behaviorResolver;

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
        when(process.isInTerminalState()).thenReturn(false);
        when(behaviorResolver.resolveBehavior(ActivityType.EXTERNAL_TASK)).thenReturn(activityBehavior);
    }

    @Test
    @DisplayName("Should return correct command type")
    void shouldReturnCorrectCommandType() {
        assertThat(handler.getCommandType()).isEqualTo(CompleteActivityCommand.class);
    }

    @Test
    @DisplayName("Should complete activity successfully when activity is provided")
    void shouldCompleteActivitySuccessfullyWhenActivityProvided() {
        var command = CompleteActivityCommand.of(activityExecution, VARIABLES);

        handler.handle(command);

        verify(behaviorResolver).resolveBehavior(ActivityType.EXTERNAL_TASK);
        verify(activityBehavior).complete(activityExecution, VARIABLES);
        verify(dispatcher, never()).dispatch(isA(FailActivityCommand.class));
    }

    @Test
    @DisplayName("Should complete activity successfully when retrieved by ID")
    void shouldCompleteActivitySuccessfullyWhenRetrievedById() {
        when(activityFactory.getById(ACTIVITY_ID)).thenReturn(activityExecution);
        var command = CompleteActivityCommand.of(ACTIVITY_ID, VARIABLES);

        handler.handle(command);

        verify(activityFactory).getById(ACTIVITY_ID);
        verify(behaviorResolver).resolveBehavior(ActivityType.EXTERNAL_TASK);
        verify(activityBehavior).complete(activityExecution, VARIABLES);
        verify(dispatcher, never()).dispatch(isA(FailActivityCommand.class));
    }

    @Test
    @DisplayName("Should not complete activity if both activity and process are in terminal state")
    void shouldNotCompleteActivityIfBothInTerminalState() {
        when(activityExecution.isInTerminalState()).thenReturn(true);
        when(process.isInTerminalState()).thenReturn(true);
        when(activityExecution.state()).thenReturn(ActivityState.COMPLETED);

        var command = CompleteActivityCommand.of(activityExecution, VARIABLES);
        handler.handle(command);

        verify(behaviorResolver, never()).resolveBehavior(any());
        verify(activityBehavior, never()).complete(any(), any());
        verify(dispatcher, never()).dispatch(any());
    }

    @Test
    @DisplayName("Should dispatch FailActivityCommand and throw ExecutionException when completion fails")
    void shouldDispatchFailActivityCommandWhenCompletionFails() {
        var command = CompleteActivityCommand.of(activityExecution, VARIABLES);
        var exception = new RuntimeException("failure");
        doThrow(exception).when(activityBehavior).complete(activityExecution, VARIABLES);

        assertThatThrownBy(() -> handler.handle(command))
                .isInstanceOf(ExecutionException.class)
                .hasMessageContaining("Activity completion failed")
                .hasCause(exception);

        verify(dispatcher).dispatch(isA(FailActivityCommand.class));
    }

    @Test
    @DisplayName("Should handle activity with null state (still process it)")
    void shouldHandleActivityWithNullState() {
        when(activityExecution.state()).thenReturn(null);
        var command = CompleteActivityCommand.of(activityExecution, VARIABLES);

        handler.handle(command);

        verify(activityBehavior).complete(activityExecution, VARIABLES);
    }

}
