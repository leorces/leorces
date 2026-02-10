package com.leorces.engine.variables.handler;

import com.leorces.common.mapper.VariablesMapper;
import com.leorces.engine.core.CommandDispatcher;
import com.leorces.engine.variables.command.EvaluateVariablesCommand;
import com.leorces.engine.variables.command.SetActivityVariablesCommand;
import com.leorces.engine.variables.command.SetVariablesCommand;
import com.leorces.model.runtime.activity.ActivityExecution;
import com.leorces.model.runtime.process.Process;
import com.leorces.model.runtime.variable.Variable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("SetActivityVariablesCommandHandler Tests")
class SetActivityVariablesCommandHandlerTest {

    @Mock
    private VariablesMapper variablesMapper;

    @Mock
    private CommandDispatcher dispatcher;

    @Mock
    private ActivityExecution activity;

    @Mock
    private Process process;

    @InjectMocks
    private SetActivityVariablesCommandHandler handler;

    @Test
    @DisplayName("should return correct command type")
    void shouldReturnCorrectCommandType() {
        // When & Then
        assertThat(handler.getCommandType()).isEqualTo(SetActivityVariablesCommand.class);
    }

    @Test
    @DisplayName("should evaluate outputs, merge with inputs and dispatch SetVariablesCommand")
    void shouldEvaluateMergeAndDispatch() {
        // Given
        when(activity.process()).thenReturn(process);

        var inputs = Map.<String, Object>of("x", 1, "same", "in");
        var outputsTemplate = Map.<String, Object>of("y", "${x}");
        when(activity.outputs()).thenReturn(outputsTemplate);

        var evaluated = List.<Variable>of();
        when(dispatcher.execute(any(EvaluateVariablesCommand.class))).thenReturn(evaluated);
        var outputs = Map.<String, Object>of("y", 2, "same", "out");
        when(variablesMapper.toMap(evaluated)).thenReturn(outputs);

        var command = SetActivityVariablesCommand.of(activity, inputs);

        // When
        handler.handle(command);

        // Then
        var captor = ArgumentCaptor.forClass(SetVariablesCommand.class);
        verify(dispatcher).dispatch(captor.capture());

        var dispatched = captor.getValue();
        assertThat(dispatched.process()).isEqualTo(process);
        assertThat(dispatched.local()).isTrue();
        assertThat(dispatched.variables())
                .containsEntry("x", 1)
                .containsEntry("y", 2)
                .containsEntry("same", "out"); // outputs override inputs

        verify(dispatcher).execute(any(EvaluateVariablesCommand.class));
        verify(variablesMapper).toMap(evaluated);
    }

}
