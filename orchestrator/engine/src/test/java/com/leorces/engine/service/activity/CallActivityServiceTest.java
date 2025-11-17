package com.leorces.engine.service.activity;

import com.leorces.engine.service.variable.VariablesService;
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
@DisplayName("CallActivityService Tests")
class CallActivityServiceTest {

    @Mock
    private VariablesService variablesService;

    @Mock
    private ExpressionEvaluator expressionEvaluator;

    @Mock
    private ActivityExecution activity;

    @Mock
    private CallActivity callActivity;

    @InjectMocks
    private CallActivityService callActivityService;

    @BeforeEach
    void setup() {
        when(activity.definition()).thenReturn(callActivity);
    }

    @Test
    @DisplayName("getInputMappings should map simple source-target variables")
    void getInputMappings_shouldMapSimpleSourceTarget() {
        // given
        var mapping = VariableMapping.builder()
                .source("input1")
                .target("mapped1")
                .build();

        Map<String, Object> scopedVars = Map.of("input1", "value1");

        when(callActivity.inputMappings()).thenReturn(List.of(mapping));
        when(callActivity.shouldProcessAllInputMappings()).thenReturn(false);
        when(variablesService.getScopedVariables(activity)).thenReturn(scopedVars);

        // when
        var result = callActivityService.getInputMappings(activity);

        // then
        assertThat(result).containsEntry("mapped1", "value1");
        verify(expressionEvaluator, never()).evaluate(anyString(), anyMap(), any());
    }

    @Test
    @DisplayName("getInputMappings should evaluate sourceExpression")
    void getInputMappings_shouldEvaluateExpression() {
        // given
        var mapping = VariableMapping.builder()
                .sourceExpression("${x + 1}")
                .target("result")
                .build();

        Map<String, Object> scopedVars = Map.of("x", 2);

        when(callActivity.inputMappings()).thenReturn(List.of(mapping));
        when(callActivity.shouldProcessAllInputMappings()).thenReturn(false);
        when(variablesService.getScopedVariables(activity)).thenReturn(scopedVars);
        when(expressionEvaluator.evaluate("${x + 1}", scopedVars, Object.class)).thenReturn(3);

        // when
        var result = callActivityService.getInputMappings(activity);

        // then
        assertThat(result).containsEntry("result", 3);
        verify(expressionEvaluator).evaluate("${x + 1}", scopedVars, Object.class);
    }

    @Test
    @DisplayName("getInputMappings should include all scoped variables when shouldProcessAllInputMappings = true")
    void getInputMappings_shouldIncludeAllVariablesWhenFlagTrue() {
        // given
        var mapping = VariableMapping.builder()
                .source("a").target("b").build();

        Map<String, Object> scopedVars = Map.of("x", 1, "y", 2);

        when(callActivity.inputMappings()).thenReturn(List.of(mapping));
        when(callActivity.shouldProcessAllInputMappings()).thenReturn(true);
        when(variablesService.getScopedVariables(activity)).thenReturn(scopedVars);

        // when
        var result = callActivityService.getInputMappings(activity);

        // then
        assertThat(result).containsKeys("x", "y", "b");
    }

    @Test
    @DisplayName("getOutputMappings should map variables from process scope")
    void getOutputMappings_shouldMapVariables() {
        // given
        var mapping = VariableMapping.builder()
                .source("out1").target("mappedOut").build();

        Map<String, Object> processVars = Map.of("out1", "val1");

        when(callActivity.outputMappings()).thenReturn(List.of(mapping));
        when(callActivity.shouldProcessAllOutputMappings()).thenReturn(false);
        when(variablesService.getProcessVariables("exec1")).thenReturn(processVars);
        when(activity.id()).thenReturn("exec1");

        // when
        var result = callActivityService.getOutputMappings(activity);

        // then
        assertThat(result).containsEntry("mappedOut", "val1");
    }

    @Test
    @DisplayName("getInputMappings should return empty map when inputMappings is null")
    void getInputMappings_shouldReturnEmptyMapWhenNull() {
        when(callActivity.inputMappings()).thenReturn(null);

        var result = callActivityService.getInputMappings(activity);

        assertThat(result).isEmpty();
        verifyNoInteractions(variablesService);
    }

    @Test
    @DisplayName("getOutputMappings should return empty map when outputMappings is null")
    void getOutputMappings_shouldReturnEmptyMapWhenNull() {
        when(callActivity.outputMappings()).thenReturn(null);

        var result = callActivityService.getOutputMappings(activity);

        assertThat(result).isEmpty();
        verifyNoInteractions(variablesService);
    }

    @Test
    @DisplayName("applyMapping should skip mappings without target")
    void shouldSkipMappingWithoutTarget() {
        var mapping = VariableMapping.builder().source("a").build();

        Map<String, Object> scopedVars = Map.of("a", "value");
        when(callActivity.inputMappings()).thenReturn(List.of(mapping));
        when(callActivity.shouldProcessAllInputMappings()).thenReturn(false);
        when(variablesService.getScopedVariables(activity)).thenReturn(scopedVars);

        var result = callActivityService.getInputMappings(activity);

        assertThat(result).isEmpty();
    }

}
