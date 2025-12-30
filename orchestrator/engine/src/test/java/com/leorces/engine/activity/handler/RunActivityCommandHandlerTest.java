package com.leorces.engine.activity.handler;

import com.leorces.engine.activity.behaviour.ActivityBehavior;
import com.leorces.engine.activity.behaviour.ActivityBehaviorResolver;
import com.leorces.engine.activity.command.RunActivityCommand;
import com.leorces.engine.core.CommandDispatcher;
import com.leorces.engine.core.CommandHandler;
import com.leorces.engine.service.activity.ActivityFactory;
import com.leorces.engine.service.variable.VariablesService;
import com.leorces.model.definition.activity.ActivityDefinition;
import com.leorces.model.definition.activity.ActivityType;
import com.leorces.model.runtime.activity.ActivityExecution;
import com.leorces.model.runtime.process.Process;
import com.leorces.model.runtime.process.ProcessState;
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
    private CommandDispatcher dispatcher;

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
        when(process.state()).thenReturn(ProcessState.ACTIVE);
        when(process.id()).thenReturn(PROC_ID);

        when(activityExecution.process()).thenReturn(process);
        when(activityExecution.processId()).thenReturn(PROC_ID);
        when(activityExecution.definitionId()).thenReturn(DEF_ID);
        when(activityExecution.type()).thenReturn(ActivityType.EXTERNAL_TASK);
        when(activityExecution.inputs()).thenReturn(Collections.emptyMap());
        when(activityExecution.toBuilder()).thenAnswer(invocation -> baseBuilder());

        when(behaviorResolver.resolveBehavior(ActivityType.EXTERNAL_TASK))
                .thenReturn(activityBehavior);

        when(variablesService.evaluate(any(ActivityExecution.class), any(Map.class)))
                .thenReturn(List.<Variable>of());
    }

    @Test
    @DisplayName("should return correct command type")
    void shouldReturnCorrectCommandType() {
        CommandHandler<RunActivityCommand> commandHandler = handler;

        assertThat(commandHandler.getCommandType())
                .isEqualTo(RunActivityCommand.class);
    }

    @Test
    @DisplayName("should run activity from command.activity()")
    void shouldRunActivityFromCommand() {
        var command = RunActivityCommand.of(activityExecution);

        handler.handle(command);

        verify(variablesService).evaluate(activityExecution, activityExecution.inputs());
        verify(activityBehavior).run(any(ActivityExecution.class));
        verifyNoInteractions(dispatcher);
    }

    @Test
    @DisplayName("should run activity from definitionId")
    void shouldRunFromDefinitionId() {
        when(activityFactory.getNewByDefinitionId(DEF_ID, PROC_ID))
                .thenReturn(activityExecution);

        var command = RunActivityCommand.of(DEF_ID, PROC_ID);

        handler.handle(command);

        verify(activityFactory).getNewByDefinitionId(DEF_ID, PROC_ID);
        verify(activityBehavior).run(any(ActivityExecution.class));
    }

    @Test
    @DisplayName("should run activity from definition + process")
    void shouldRunFromDefinitionAndProcess() {
        when(activityFactory.createActivity(activityDefinition, process))
                .thenReturn(activityExecution);

        var command = RunActivityCommand.of(process, activityDefinition);

        handler.handle(command);

        verify(activityFactory).createActivity(activityDefinition, process);
        verify(activityBehavior).run(any(ActivityExecution.class));
    }

    @Test
    @DisplayName("should NOT run activity if process is in terminal state")
    void shouldNotRunIfProcessInTerminalState() {
        when(process.isInTerminalState()).thenReturn(true);

        var command = RunActivityCommand.of(activityExecution);

        handler.handle(command);

        verifyNoInteractions(
                variablesService,
                behaviorResolver,
                activityBehavior,
                dispatcher
        );
    }

    @Test
    @DisplayName("should resolve process incident if process is in INCIDENT state")
    void shouldResolveProcessIncident() {
        when(process.isIncident()).thenReturn(true);

        var command = RunActivityCommand.of(activityExecution);

        handler.handle(command);

        verify(dispatcher).dispatch(any());
        verify(activityBehavior).run(any(ActivityExecution.class));
    }

    private ActivityExecution.ActivityExecutionBuilder baseBuilder() {
        return ActivityExecution.builder()
                .process(process)
                .definitionId(DEF_ID);
    }

}
