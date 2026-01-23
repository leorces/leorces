package com.leorces.engine.service.variable;

import com.leorces.common.mapper.VariablesMapper;
import com.leorces.juel.ExpressionEvaluator;
import com.leorces.model.runtime.activity.ActivityExecution;
import com.leorces.model.runtime.variable.Variable;
import com.leorces.persistence.VariablePersistence;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("VariablesService Tests")
class VariablesServiceTest {

    private static final String PROCESS_ID = "process-123";
    private static final String VARIABLE_NAME = "varName";
    private static final String VARIABLE_VALUE = "varValue";
    private static final String EXPRESSION_VALUE = "${variable}";
    private static final List<String> SCOPE = List.of("scope-1", "scope-2");

    @Mock
    private ExpressionEvaluator expressionEvaluator;

    @Mock
    private VariablesMapper variablesMapper;

    @Mock
    private VariablePersistence variablePersistence;

    @Mock
    private ActivityExecution activityExecution;

    @InjectMocks
    private VariablesService variablesService;

    private List<Variable> variables;
    private Map<String, Object> variablesMap;

    @BeforeEach
    void setUp() {
        variables = List.of(createVariable(VARIABLE_NAME, VARIABLE_VALUE));
        variablesMap = Map.of(VARIABLE_NAME, VARIABLE_VALUE);

        when(activityExecution.processId()).thenReturn(PROCESS_ID);
        when(activityExecution.scope()).thenReturn(SCOPE);
    }

    @Test
    @DisplayName("Should get scoped variables successfully")
    void shouldGetScopedVariablesSuccessfully() {
        // Given
        when(variablePersistence.findInScope(PROCESS_ID, SCOPE)).thenReturn(variables);
        when(variablesMapper.toMap(variables, SCOPE)).thenReturn(variablesMap);

        // When
        var result = variablesService.getScopedVariables(activityExecution);

        // Then
        assertThat(result).isEqualTo(variablesMap);
        verify(variablePersistence).findInScope(PROCESS_ID, SCOPE);
        verify(variablesMapper).toMap(variables, SCOPE);
    }

    @Test
    @DisplayName("Should get process variables successfully")
    void shouldGetProcessVariablesSuccessfully() {
        // Given
        when(variablePersistence.findInProcess(PROCESS_ID)).thenReturn(variables);
        when(variablesMapper.toMap(variables)).thenReturn(variablesMap);

        // When
        var result = variablesService.getProcessVariables(PROCESS_ID);

        // Then
        assertThat(result).isEqualTo(variablesMap);
        verify(variablePersistence).findInProcess(PROCESS_ID);
        verify(variablesMapper).toMap(variables);
    }

    @Test
    @DisplayName("Should convert variables map to list")
    void shouldConvertVariablesMapToList() {
        // Given
        when(variablesMapper.map(variablesMap)).thenReturn(variables);

        // When
        var result = variablesService.toList(variablesMap);

        // Then
        assertThat(result).isEqualTo(variables);
        verify(variablesMapper).map(variablesMap);
    }

    @Test
    @DisplayName("Should convert variables list to map")
    void shouldConvertVariablesListToMap() {
        // Given
        when(variablesMapper.toMap(variables)).thenReturn(variablesMap);

        // When
        var result = variablesService.toMap(variables);

        // Then
        assertThat(result).isEqualTo(variablesMap);
        verify(variablesMapper).toMap(variables);
    }

    @Test
    @DisplayName("Should return empty list when evaluating null variables")
    void shouldReturnEmptyListWhenEvaluatingNullVariables() {
        // When
        var result = variablesService.evaluate(activityExecution, null);

        // Then
        assertThat(result).isEmpty();
        verifyNoInteractions(expressionEvaluator, variablesMapper, variablePersistence);
    }

    @Test
    @DisplayName("Should return empty list when evaluating empty variables")
    void shouldReturnEmptyListWhenEvaluatingEmptyVariables() {
        // When
        var result = variablesService.evaluate(activityExecution, Collections.emptyMap());

        // Then
        assertThat(result).isEmpty();
        verifyNoInteractions(expressionEvaluator, variablesMapper, variablePersistence);
    }

    @Test
    @DisplayName("Should evaluate variables without expressions")
    void shouldEvaluateVariablesWithoutExpressions() {
        // Given
        when(expressionEvaluator.isExpression(VARIABLE_VALUE)).thenReturn(false);
        when(variablesMapper.map(variablesMap)).thenReturn(variables);

        // When
        var result = variablesService.evaluate(activityExecution, variablesMap);

        // Then
        assertThat(result).isEqualTo(variables);
        verify(expressionEvaluator).isExpression(VARIABLE_VALUE);
        verify(variablesMapper).map(variablesMap);
    }

    @Test
    @DisplayName("Should evaluate variables with expressions")
    void shouldEvaluateVariablesWithExpressions() {
        // Given
        Map<String, Object> expressionMap = Map.of("key", EXPRESSION_VALUE);
        Map<String, Object> evaluatedMap = Map.of("key", "evaluatedValue");
        var scopedVariables = List.of(createVariable("scopeVar", "scopeValue"));
        Map<String, Object> scopedVariablesMap = Map.of("scopeVar", "scopeValue");
        var evaluatedVariables = List.of(createVariable("key", "evaluatedValue"));

        when(expressionEvaluator.isExpression(EXPRESSION_VALUE)).thenReturn(true);
        when(variablePersistence.findInScope(PROCESS_ID, SCOPE)).thenReturn(scopedVariables);
        when(variablesMapper.toMap(scopedVariables, SCOPE)).thenReturn(scopedVariablesMap);
        when(expressionEvaluator.evaluate(expressionMap, scopedVariablesMap)).thenReturn(evaluatedMap);
        when(variablesMapper.map(evaluatedMap)).thenReturn(evaluatedVariables);

        // When
        var result = variablesService.evaluate(activityExecution, expressionMap);

        // Then
        assertThat(result).isEqualTo(evaluatedVariables);
        verify(expressionEvaluator).isExpression(EXPRESSION_VALUE);
        verify(variablePersistence).findInScope(PROCESS_ID, SCOPE);
        verify(variablesMapper).toMap(scopedVariables, SCOPE);
        verify(expressionEvaluator).evaluate(expressionMap, scopedVariablesMap);
        verify(variablesMapper).map(evaluatedMap);
    }

    @Test
    @DisplayName("Should handle mixed variables with and without expressions")
    void shouldHandleMixedVariablesWithAndWithoutExpressions() {
        // Given
        Map<String, Object> mixedMap = Map.of(
                "regularVar", "regularValue",
                "expressionVar", EXPRESSION_VALUE
        );
        Map<String, Object> evaluatedMap = Map.of(
                "regularVar", "regularValue",
                "expressionVar", "evaluatedValue"
        );
        var scopedVariables = List.of(createVariable("scopeVar", "scopeValue"));
        Map<String, Object> scopedVariablesMap = Map.of("scopeVar", "scopeValue");
        var evaluatedVariables = List.of(
                createVariable("regularVar", "regularValue"),
                createVariable("expressionVar", "evaluatedValue")
        );

        when(expressionEvaluator.isExpression("regularValue")).thenReturn(false);
        when(expressionEvaluator.isExpression(EXPRESSION_VALUE)).thenReturn(true);
        when(variablePersistence.findInScope(PROCESS_ID, SCOPE)).thenReturn(scopedVariables);
        when(variablesMapper.toMap(scopedVariables, SCOPE)).thenReturn(scopedVariablesMap);
        when(expressionEvaluator.evaluate(mixedMap, scopedVariablesMap)).thenReturn(evaluatedMap);
        when(variablesMapper.map(evaluatedMap)).thenReturn(evaluatedVariables);

        // When
        var result = variablesService.evaluate(activityExecution, mixedMap);

        // Then
        assertThat(result).isEqualTo(evaluatedVariables);
        verify(variablePersistence).findInScope(PROCESS_ID, SCOPE);
        verify(expressionEvaluator).evaluate(mixedMap, scopedVariablesMap);
    }

    @Test
    @DisplayName("Should handle variables with non-string values")
    void shouldHandleVariablesWithNonStringValues() {
        // Given
        Map<String, Object> mapWithNumbers = Map.of(
                "numberVar", 123,
                "booleanVar", true
        );
        var resultVariables = List.of(
                createVariable("numberVar", 123),
                createVariable("booleanVar", true)
        );

        when(variablesMapper.map(mapWithNumbers)).thenReturn(resultVariables);

        // When
        var result = variablesService.evaluate(activityExecution, mapWithNumbers);

        // Then
        assertThat(result).isEqualTo(resultVariables);
        verify(variablesMapper).map(mapWithNumbers);
    }

    private Variable createVariable(String varKey, Object varValue) {
        return Variable.builder()
                .varKey(varKey)
                .varValue(String.valueOf(varValue))
                .build();
    }

}