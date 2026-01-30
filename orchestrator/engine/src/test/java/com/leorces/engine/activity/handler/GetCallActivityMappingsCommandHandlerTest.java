package com.leorces.engine.activity.handler;

import com.leorces.engine.activity.command.GetCallActivityMappingsCommand;
import com.leorces.engine.core.CommandDispatcher;
import com.leorces.engine.variables.command.GetProcessVariablesCommand;
import com.leorces.juel.ExpressionEvaluator;
import com.leorces.model.definition.VariableMapping;
import com.leorces.model.definition.activity.subprocess.CallActivity;
import com.leorces.model.runtime.activity.ActivityExecution;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("GetCallActivityMappingsCommandHandler Tests")
class GetCallActivityMappingsCommandHandlerTest {

    @Mock
    private CommandDispatcher dispatcher;

    @Mock
    private ExpressionEvaluator expressionEvaluator;

    @Mock
    private ActivityExecution activity;

    @Mock
    private CallActivity callActivity;

    @InjectMocks
    private GetCallActivityMappingsCommandHandler handler;

    @BeforeEach
    void setup() {
        lenient().when(activity.definition()).thenReturn(callActivity);
    }

    @Test
    @DisplayName("execute should return input mappings with simple source-target variables")
    void execute_shouldMapSimpleSourceTargetInput() {
        // given
        var mapping = VariableMapping.builder()
                .source("input1")
                .target("mapped1")
                .build();
        var scopedVariables = Map.<String, Object>of("input1", "value1");
        var command = GetCallActivityMappingsCommand.input(activity, scopedVariables);

        when(callActivity.inputMappings()).thenReturn(List.of(mapping));
        when(callActivity.shouldProcessAllInputMappings()).thenReturn(false);

        // when
        var result = handler.execute(command);

        // then
        assertThat(result).containsEntry("mapped1", "value1");
        verifyNoInteractions(expressionEvaluator);
    }

    @Test
    @DisplayName("execute should return input mappings evaluating sourceExpression")
    void execute_shouldEvaluateExpressionInput() {
        // given
        var mapping = VariableMapping.builder()
                .sourceExpression("${x + 1}")
                .target("result")
                .build();
        var scopedVariables = Map.<String, Object>of("x", 2);
        var command = GetCallActivityMappingsCommand.input(activity, scopedVariables);

        when(callActivity.inputMappings()).thenReturn(List.of(mapping));
        when(callActivity.shouldProcessAllInputMappings()).thenReturn(false);
        when(expressionEvaluator.evaluate(eq("${x + 1}"), anyMap(), eq(Object.class))).thenReturn(3);

        // when
        var result = handler.execute(command);

        // then
        assertThat(result).containsEntry("result", 3);
        verify(expressionEvaluator).evaluate(eq("${x + 1}"), anyMap(), eq(Object.class));
    }

    @Test
    @DisplayName("execute should include all scoped variables in input when flag is true")
    void execute_shouldIncludeAllVariablesInputWhenFlagTrue() {
        // given
        var mapping = VariableMapping.builder()
                .source("a").target("b").build();
        var scopedVariables = Map.<String, Object>of("x", 1, "y", 2);
        var command = GetCallActivityMappingsCommand.input(activity, scopedVariables);

        when(callActivity.inputMappings()).thenReturn(List.of(mapping));
        when(callActivity.shouldProcessAllInputMappings()).thenReturn(true);

        // when
        var result = handler.execute(command);

        // then
        assertThat(result).containsKeys("x", "y", "b");
    }

    @Test
    @DisplayName("execute should return output mappings from process scope")
    void execute_shouldReturnOutputMappings() {
        // given
        var mapping = VariableMapping.builder()
                .source("out1").target("mappedOut").build();
        var processVariables = Map.<String, Object>of("out1", "val1");
        var command = GetCallActivityMappingsCommand.output(activity);

        when(activity.id()).thenReturn("exec1");
        when(callActivity.outputMappings()).thenReturn(List.of(mapping));
        when(callActivity.shouldProcessAllOutputMappings()).thenReturn(false);
        when(dispatcher.execute(GetProcessVariablesCommand.of("exec1"))).thenReturn(processVariables);

        // when
        var result = handler.execute(command);

        // then
        assertThat(result).containsEntry("mappedOut", "val1");
    }

    @Test
    @DisplayName("execute should return empty map when inputMappings is null")
    void execute_shouldReturnEmptyMapWhenInputMappingsNull() {
        // given
        var command = GetCallActivityMappingsCommand.input(activity, Map.of());

        when(callActivity.inputMappings()).thenReturn(null);

        // when
        var result = handler.execute(command);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("execute should return empty map when outputMappings is null")
    void execute_shouldReturnEmptyMapWhenOutputMappingsNull() {
        // given
        var command = GetCallActivityMappingsCommand.output(activity);

        when(callActivity.outputMappings()).thenReturn(null);

        // when
        var result = handler.execute(command);

        // then
        assertThat(result).isEmpty();
        verify(dispatcher, never()).execute(any());
    }

    @Test
    @DisplayName("execute should skip input mappings without target")
    void execute_shouldSkipInputMappingWithoutTarget() {
        // given
        var mapping = VariableMapping.builder().source("a").build();
        var scopedVariables = Map.<String, Object>of("a", "value");
        var command = GetCallActivityMappingsCommand.input(activity, scopedVariables);

        when(callActivity.inputMappings()).thenReturn(List.of(mapping));
        when(callActivity.shouldProcessAllInputMappings()).thenReturn(false);

        // when
        var result = handler.execute(command);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("getCommandType should return GetCallActivityMappingsCommand class")
    void getCommandType_shouldReturnCorrectClass() {
        // When & Then
        assertThat(handler.getCommandType()).isEqualTo(GetCallActivityMappingsCommand.class);
    }

}
