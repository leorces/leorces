package com.leorces.engine.variables.handler;

import com.leorces.common.mapper.VariablesMapper;
import com.leorces.engine.core.CommandDispatcher;
import com.leorces.engine.core.CommandHandler;
import com.leorces.engine.correlation.command.CorrelateVariablesCommand;
import com.leorces.engine.variables.command.SetVariablesCommand;
import com.leorces.model.runtime.activity.ActivityExecution;
import com.leorces.model.runtime.process.Process;
import com.leorces.model.runtime.variable.Variable;
import com.leorces.persistence.ActivityPersistence;
import com.leorces.persistence.ProcessPersistence;
import com.leorces.persistence.VariablePersistence;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SetVariablesCommandHandler Tests")
class SetVariablesCommandHandlerTest {

    private static final String PROCESS_ID = "proc-1";
    private static final String ACTIVITY_ID = "act-1";
    private static final String DEF_ID = "def-1";

    @Mock
    private VariablePersistence variablePersistence;

    @Mock
    private ProcessPersistence processPersistence;

    @Mock
    private ActivityPersistence activityPersistence;

    @Mock
    private VariablesMapper variablesMapper;

    @Mock
    private CommandDispatcher dispatcher;

    @Mock
    private Process process;

    @Mock
    private ActivityExecution activity;

    @InjectMocks
    private SetVariablesCommandHandler handler;

    @Test
    @DisplayName("should return correct command type")
    void shouldReturnCorrectCommandType() {
        // When & Then
        CommandHandler<SetVariablesCommand> commandHandler = handler;
        assertThat(commandHandler.getCommandType()).isEqualTo(SetVariablesCommand.class);
    }

    @Test
    @DisplayName("should do nothing when variables are empty")
    void shouldDoNothingOnEmptyVariables() {
        // Given
        var command = SetVariablesCommand.of(PROCESS_ID, Collections.emptyMap(), true);

        // When
        handler.handle(command);

        // Then
        verifyNoInteractions(variablePersistence, processPersistence, activityPersistence, variablesMapper, dispatcher);
    }

    @Test
    @DisplayName("should set process variables and correlate when process provided and active")
    void shouldSetProcessVariablesWhenProcessProvided() {
        // Given
        when(process.variables()).thenReturn(List.of());

        Map<String, Object> input = Map.of("a", 1, "b", true);

        var incoming = List.of(
                Variable.builder().varKey("a").varValue("1").type("integer").build(),
                Variable.builder().varKey("b").varValue("true").type("boolean").build()
        );
        when(variablesMapper.map(input)).thenReturn(incoming);

        // map new variables to process context
        when(variablesMapper.map(eq(process), any(Variable.class)))
                .thenAnswer(inv -> ((Variable) inv.getArgument(1)).toBuilder()
                        .processId(PROCESS_ID)
                        .executionId(PROCESS_ID)
                        .executionDefinitionId(DEF_ID)
                        .build());

        // persistence returns same as merged for correlation
        when(variablePersistence.update(anyList())).thenAnswer(inv -> inv.getArgument(0));

        var command = SetVariablesCommand.of(process, input);

        // When
        handler.handle(command);

        // Then
        var cmdCaptor = ArgumentCaptor.forClass(CorrelateVariablesCommand.class);
        verify(dispatcher).dispatch(cmdCaptor.capture());
        var dispatched = cmdCaptor.getValue();
        assertThat(dispatched.process()).isEqualTo(process);
        assertThat(dispatched.variables()).hasSize(2);
        verify(variablePersistence).update(anyList());
    }

    @Test
    @DisplayName("should update activity variables locally and correlate")
    void shouldUpdateActivityVariablesLocal() {
        // Given
        when(activityPersistence.findById(ACTIVITY_ID)).thenReturn(Optional.of(activity));
        when(activity.id()).thenReturn(ACTIVITY_ID);
        when(activity.process()).thenReturn(process);

        // existing vars: one local (will be updated), one non-local (ignored)
        var localExisting = Variable.builder()
                .processId(PROCESS_ID)
                .executionId(ACTIVITY_ID)
                .executionDefinitionId(DEF_ID)
                .varKey("x")
                .varValue("old")
                .type("string")
                .build();
        var nonLocal = Variable.builder()
                .processId(PROCESS_ID)
                .executionId("other")
                .executionDefinitionId("other-def")
                .varKey("y")
                .varValue("1")
                .type("integer")
                .build();
        when(activity.variables()).thenReturn(List.of(localExisting, nonLocal));

        Map<String, Object> input = Map.of("x", "new", "z", 10);

        var incoming = List.of(
                Variable.builder().varKey("x").varValue("new").type("string").build(),
                Variable.builder().varKey("z").varValue("10").type("integer").build()
        );
        when(variablesMapper.map(input)).thenReturn(incoming);

        // new variable mapped to activity context
        when(variablesMapper.map(eq(activity), any(Variable.class)))
                .thenAnswer(inv -> ((Variable) inv.getArgument(1)).toBuilder()
                        .processId(PROCESS_ID)
                        .executionId(ACTIVITY_ID)
                        .executionDefinitionId(DEF_ID)
                        .build());

        when(variablePersistence.update(anyList())).thenAnswer(inv -> inv.getArgument(0));

        var command = SetVariablesCommand.of(ACTIVITY_ID, input, true);

        // When
        handler.handle(command);

        // Then
        verify(variablePersistence).update(anyList());
        verify(dispatcher).dispatch(any());
    }

}
