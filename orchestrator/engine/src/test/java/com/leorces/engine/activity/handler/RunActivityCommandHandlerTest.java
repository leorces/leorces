package com.leorces.engine.activity.handler;

import com.leorces.engine.activity.ActivityFactory;
import com.leorces.engine.activity.behaviour.ActivityBehavior;
import com.leorces.engine.activity.behaviour.ActivityBehaviorResolver;
import com.leorces.engine.activity.command.RunActivityCommand;
import com.leorces.engine.core.CommandHandler;
import com.leorces.engine.variables.VariablesService;
import com.leorces.model.definition.activity.ActivityDefinition;
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

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("RunActivityCommandHandler Tests")
class RunActivityCommandHandlerTest {

    private static final String DEF_ID = "def-1";
    private static final String PROC_ID = "proc-1";

    @Mock
    private VariablesService variablesService;

    @Mock
    private ActivityBehaviorResolver behaviorResolver;

    @Mock
    private ActivityFactory activityFactory;

    @Mock
    private ActivityBehavior activityBehavior;

    @Mock
    private ActivityDefinition activityDefinition;

    @Mock
    private Process process;

    @Mock
    private ActivityExecution activityExecution;

    @InjectMocks
    private RunActivityCommandHandler handler;

    @BeforeEach
    void setUp() {
        when(process.isInTerminalState()).thenReturn(false);

        when(activityExecution.type()).thenReturn(ActivityType.EXTERNAL_TASK);
        when(activityExecution.definitionId()).thenReturn(DEF_ID);
        when(activityExecution.processId()).thenReturn(PROC_ID);
        when(activityExecution.process()).thenReturn(process);
        when(activityExecution.inputs()).thenReturn(Collections.emptyMap());

        when(behaviorResolver.resolveBehavior(ActivityType.EXTERNAL_TASK)).thenReturn(activityBehavior);
        when(variablesService.evaluate(any(), any())).thenReturn(List.of());
        when(activityExecution.toBuilder()).thenReturn(ActivityExecution.builder());
    }

    @Test
    @DisplayName("Should return correct command type")
    void shouldReturnCorrectCommandType() {
        CommandHandler<RunActivityCommand> ch = handler;
        assertThat(ch.getCommandType()).isEqualTo(RunActivityCommand.class);
    }

    @Test
    @DisplayName("Should run activity from command.activity()")
    void shouldRunActivityFromCommand() {
        var command = RunActivityCommand.of(activityExecution);

        handler.handle(command);

        verify(variablesService).evaluate(activityExecution, activityExecution.inputs());
        verify(activityBehavior).run(any(ActivityExecution.class));
    }

    @Test
    @DisplayName("Should run activity from definitionId when activity is null")
    void shouldRunFromDefinitionId() {
        when(activityFactory.getNewByDefinitionId(DEF_ID, PROC_ID)).thenReturn(activityExecution);
        var command = RunActivityCommand.of(DEF_ID, PROC_ID);

        handler.handle(command);

        verify(activityFactory).getNewByDefinitionId(DEF_ID, PROC_ID);
        verify(activityBehavior).run(any(ActivityExecution.class));
    }

    @Test
    @DisplayName("Should run activity from definition + process when both provided")
    void shouldRunFromDefinitionAndProcess() {
        when(activityFactory.createActivity(activityDefinition, process)).thenReturn(activityExecution);
        var command = RunActivityCommand.of(process, activityDefinition);

        handler.handle(command);

        verify(activityFactory).createActivity(activityDefinition, process);
        verify(activityBehavior).run(any(ActivityExecution.class));
    }

    @Test
    @DisplayName("Should not run activity if process is in terminal state")
    void shouldNotRunIfProcessInTerminalState() {
        when(process.isInTerminalState()).thenReturn(true);
        var command = RunActivityCommand.of(activityExecution);

        handler.handle(command);

        verifyNoInteractions(variablesService, behaviorResolver, activityBehavior);
    }

}
