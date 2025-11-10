package com.leorces.engine.activity.handler;

import com.leorces.engine.activity.ActivityFactory;
import com.leorces.engine.activity.behaviour.ActivityBehavior;
import com.leorces.engine.activity.behaviour.ActivityBehaviorResolver;
import com.leorces.engine.activity.command.TerminateActivityCommand;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("TerminateActivityCommandHandler Tests")
class TerminateActivityCommandHandlerTest {

    private static final String ACTIVITY_ID = "activity-id";
    private static final String PROCESS_ID = "process-id";
    private static final String DEFINITION_ID = "definition-id";

    @Mock
    private ActivityBehaviorResolver behaviorResolver;

    @Mock
    private ActivityFactory activityFactory;

    @Mock
    private ActivityBehavior activityBehavior;

    @Mock
    private ActivityExecution activityExecution;

    @Mock
    private Process process;

    @InjectMocks
    private TerminateActivityCommandHandler handler;

    @BeforeEach
    void setUp() {
        when(activityExecution.id()).thenReturn(ACTIVITY_ID);
        when(activityExecution.processId()).thenReturn(PROCESS_ID);
        when(activityExecution.definitionId()).thenReturn(DEFINITION_ID);
        when(activityExecution.type()).thenReturn(ActivityType.EXTERNAL_TASK);
        when(activityExecution.process()).thenReturn(process);
        when(behaviorResolver.resolveBehavior(ActivityType.EXTERNAL_TASK)).thenReturn(activityBehavior);

        // по умолчанию обе сущности не в терминальном состоянии
        when(activityExecution.isInTerminalState()).thenReturn(false);
        when(process.isInTerminalState()).thenReturn(false);
    }

    @Test
    @DisplayName("Should return correct command type")
    void shouldReturnCorrectCommandType() {
        assertThat(handler.getCommandType()).isEqualTo(TerminateActivityCommand.class);
    }

    @Test
    @DisplayName("Should terminate provided activity without interruption")
    void shouldTerminateProvidedActivityWithoutInterruption() {
        // given
        var command = TerminateActivityCommand.of(activityExecution, false);

        // when
        handler.handle(command);

        // then
        verify(behaviorResolver).resolveBehavior(ActivityType.EXTERNAL_TASK);
        verify(activityBehavior).terminate(activityExecution, false);
    }

    @Test
    @DisplayName("Should terminate provided activity with interruption")
    void shouldTerminateProvidedActivityWithInterruption() {
        // given
        var command = TerminateActivityCommand.of(activityExecution, true);

        // when
        handler.handle(command);

        // then
        verify(behaviorResolver).resolveBehavior(ActivityType.EXTERNAL_TASK);
        verify(activityBehavior).terminate(activityExecution, true);
    }

    @Test
    @DisplayName("Should fetch activity by ID when only activityId provided")
    void shouldFetchActivityByIdWhenOnlyActivityIdProvided() {
        // given
        when(activityFactory.getById(ACTIVITY_ID)).thenReturn(activityExecution);
        var command = TerminateActivityCommand.of(ACTIVITY_ID, false);

        // when
        handler.handle(command);

        // then
        verify(activityFactory).getById(ACTIVITY_ID);
        verify(behaviorResolver).resolveBehavior(ActivityType.EXTERNAL_TASK);
        verify(activityBehavior).terminate(activityExecution, false);
    }

    @Test
    @DisplayName("Should fetch activity by definitionId and processId if no ID or activity provided")
    void shouldFetchByDefinitionIdAndProcessIdIfNoIdOrActivityProvided() {
        // given
        when(activityFactory.getByDefinitionId(DEFINITION_ID, PROCESS_ID)).thenReturn(activityExecution);
        var command = TerminateActivityCommand.of(PROCESS_ID, DEFINITION_ID, false);

        // when
        handler.handle(command);

        // then
        verify(activityFactory).getByDefinitionId(DEFINITION_ID, PROCESS_ID);
        verify(behaviorResolver).resolveBehavior(ActivityType.EXTERNAL_TASK);
        verify(activityBehavior).terminate(activityExecution, false);
    }

    @Test
    @DisplayName("Should skip termination if activity or process is in terminal state")
    void shouldSkipIfActivityOrProcessInTerminalState() {
        // given
        when(activityExecution.isInTerminalState()).thenReturn(true);
        when(process.isInTerminalState()).thenReturn(true);
        var command = TerminateActivityCommand.of(activityExecution, false);

        // when
        handler.handle(command);

        // then
        verify(activityBehavior, never()).terminate(any(), anyBoolean());
        verify(behaviorResolver, never()).resolveBehavior(any());
    }

}
