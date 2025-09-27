package com.leorces.engine.variables;

import com.leorces.common.mapper.VariablesMapper;
import com.leorces.juel.ExpressionEvaluator;
import com.leorces.model.runtime.activity.ActivityExecution;
import com.leorces.model.runtime.variable.Variable;
import com.leorces.persistence.VariablePersistence;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VariableEvaluationServiceTest {

    private static final String PROCESS_ID = "process123";
    private static final String ACTIVITY_ID = "activity456";
    private static final String VARIABLE_NAME = "testVar";
    private static final String VARIABLE_VALUE = "testValue";
    private static final String EXPRESSION_VALUE = "${var1 + var2}";
    private static final String EVALUATED_VALUE = "result";

    @Mock
    private VariablesMapper variablesMapper;

    @Mock
    private VariablePersistence variablePersistence;

    @Mock
    private ExpressionEvaluator expressionEvaluator;

    @InjectMocks
    private VariableEvaluationService variableEvaluationService;

    @Test
    @DisplayName("Should return empty list when variables map is null")
    void shouldReturnEmptyListWhenVariablesMapIsNull() {
        // Given
        var activity = createActivityExecution();
        Map<String, Object> variables = null;

        // When
        var result = variableEvaluationService.evaluate(activity, variables);

        // Then
        assertThat(result).isEmpty();
        verify(variablesMapper, never()).map(anyMap());
        verify(expressionEvaluator, never()).isExpression(anyString());
    }

    @Test
    @DisplayName("Should return empty list when variables map is empty")
    void shouldReturnEmptyListWhenVariablesMapIsEmpty() {
        // Given
        var activity = createActivityExecution();
        var variables = new HashMap<String, Object>();

        // When
        var result = variableEvaluationService.evaluate(activity, variables);

        // Then
        assertThat(result).isEmpty();
        verify(variablesMapper, never()).map(anyMap());
        verify(expressionEvaluator, never()).isExpression(anyString());
    }

    @Test
    @DisplayName("Should map variables directly when no expressions present")
    void shouldMapVariablesDirectlyWhenNoExpressionsPresent() {
        // Given
        var activity = createActivityExecution();
        var variables = new HashMap<String, Object>();
        variables.put(VARIABLE_NAME, VARIABLE_VALUE);
        var expectedVariable = createVariable();
        var expectedVariables = List.of(expectedVariable);

        when(expressionEvaluator.isExpression(VARIABLE_VALUE)).thenReturn(false);
        when(variablesMapper.map(variables)).thenReturn(expectedVariables);

        // When
        var result = variableEvaluationService.evaluate(activity, variables);

        // Then
        assertThat(result).isEqualTo(expectedVariables);
        verify(expressionEvaluator).isExpression(VARIABLE_VALUE);
        verify(variablesMapper).map(variables);
        verify(expressionEvaluator, never()).evaluate(anyMap(), anyMap());
    }

    @Test
    @DisplayName("Should evaluate expressions when expressions are present")
    void shouldEvaluateExpressionsWhenExpressionsArePresent() {
        // Given
        var activity = createActivityExecution();
        var variables = new HashMap<String, Object>();
        variables.put(VARIABLE_NAME, EXPRESSION_VALUE);
        var existingVariables = new HashMap<String, Object>();
        existingVariables.put("var1", 10);
        existingVariables.put("var2", 20);
        var evaluatedVariables = new HashMap<String, Object>();
        evaluatedVariables.put(VARIABLE_NAME, EVALUATED_VALUE);
        var expectedVariable = createVariable();
        var expectedVariables = List.of(expectedVariable);
        var scope = List.of(ACTIVITY_ID);
        var persistedVariables = List.of(createVariable());

        when(expressionEvaluator.isExpression(EXPRESSION_VALUE)).thenReturn(true);
        when(variablePersistence.findAll(PROCESS_ID, scope)).thenReturn(persistedVariables);
        when(variablesMapper.toMap(persistedVariables, scope)).thenReturn(existingVariables);
        when(expressionEvaluator.evaluate(variables, existingVariables)).thenReturn(evaluatedVariables);
        when(variablesMapper.map(evaluatedVariables)).thenReturn(expectedVariables);

        // When
        var result = variableEvaluationService.evaluate(activity, variables);

        // Then
        assertThat(result).isEqualTo(expectedVariables);
        verify(expressionEvaluator).isExpression(EXPRESSION_VALUE);
        verify(variablePersistence).findAll(PROCESS_ID, scope);
        verify(variablesMapper).toMap(persistedVariables, scope);
        verify(expressionEvaluator).evaluate(variables, existingVariables);
        verify(variablesMapper).map(evaluatedVariables);
    }

    @Test
    @DisplayName("Should handle mixed variables with and without expressions")
    void shouldHandleMixedVariablesWithAndWithoutExpressions() {
        // Given
        var activity = createActivityExecution();
        var variables = new HashMap<String, Object>();
        variables.put("simpleVar", "simpleValue");
        variables.put("expressionVar", EXPRESSION_VALUE);
        var existingVariables = new HashMap<String, Object>();
        existingVariables.put("var1", 10);
        existingVariables.put("var2", 20);
        var evaluatedVariables = new HashMap<String, Object>();
        evaluatedVariables.put("expressionVar", EVALUATED_VALUE);
        evaluatedVariables.put("simpleVar", "simpleValue");
        var expectedVariable = createVariable();
        var expectedVariables = List.of(expectedVariable);
        var scope = List.of(ACTIVITY_ID);
        var persistedVariables = List.of(createVariable());

        when(expressionEvaluator.isExpression("simpleValue")).thenReturn(false);
        when(expressionEvaluator.isExpression(EXPRESSION_VALUE)).thenReturn(true);
        when(variablePersistence.findAll(PROCESS_ID, scope)).thenReturn(persistedVariables);
        when(variablesMapper.toMap(persistedVariables, scope)).thenReturn(existingVariables);
        when(expressionEvaluator.evaluate(variables, existingVariables)).thenReturn(evaluatedVariables);
        when(variablesMapper.map(evaluatedVariables)).thenReturn(expectedVariables);

        // When
        var result = variableEvaluationService.evaluate(activity, variables);

        // Then
        assertThat(result).isEqualTo(expectedVariables);
        verify(expressionEvaluator).isExpression("simpleValue");
        verify(expressionEvaluator).isExpression(EXPRESSION_VALUE);
        verify(variablePersistence).findAll(PROCESS_ID, scope);
        verify(variablesMapper).toMap(persistedVariables, scope);
        verify(expressionEvaluator).evaluate(variables, existingVariables);
        verify(variablesMapper).map(evaluatedVariables);
    }

    @Test
    @DisplayName("Should get variables as map for activity")
    void shouldGetVariablesAsMapForActivity() {
        // Given
        var activity = createActivityExecution();
        var scope = List.of(ACTIVITY_ID);
        var persistedVariables = List.of(createVariable());
        var expectedMap = new HashMap<String, Object>();
        expectedMap.put(VARIABLE_NAME, VARIABLE_VALUE);

        when(variablePersistence.findAll(PROCESS_ID, scope)).thenReturn(persistedVariables);
        when(variablesMapper.toMap(persistedVariables, scope)).thenReturn(expectedMap);

        // When
        var result = variableEvaluationService.getVariablesAsMap(activity);

        // Then
        assertThat(result).isEqualTo(expectedMap);
        verify(variablePersistence).findAll(PROCESS_ID, scope);
        verify(variablesMapper).toMap(persistedVariables, scope);
    }

    @Test
    @DisplayName("Should handle non-string values when checking for expressions")
    void shouldHandleNonStringValuesWhenCheckingForExpressions() {
        // Given
        var activity = createActivityExecution();
        var variables = new HashMap<String, Object>();
        variables.put("stringVar", "stringValue");
        variables.put("intVar", 123);
        variables.put("boolVar", true);
        var expectedVariable = createVariable();
        var expectedVariables = List.of(expectedVariable);

        when(expressionEvaluator.isExpression("stringValue")).thenReturn(false);
        when(variablesMapper.map(variables)).thenReturn(expectedVariables);

        // When
        var result = variableEvaluationService.evaluate(activity, variables);

        // Then
        assertThat(result).isEqualTo(expectedVariables);
        verify(expressionEvaluator).isExpression("stringValue");
        verify(variablesMapper).map(variables);
        verify(expressionEvaluator, never()).evaluate(anyMap(), anyMap());
    }

    private ActivityExecution createActivityExecution() {
        var mockActivity = mock(ActivityExecution.class, withSettings().lenient());
        org.mockito.Mockito.when(mockActivity.id()).thenReturn(ACTIVITY_ID);
        org.mockito.Mockito.when(mockActivity.processId()).thenReturn(PROCESS_ID);
        org.mockito.Mockito.when(mockActivity.scope()).thenReturn(List.of(ACTIVITY_ID));
        return mockActivity;
    }

    private Variable createVariable() {
        return Variable.builder()
                .varKey(VARIABLE_NAME)
                .varValue(VARIABLE_VALUE)
                .build();
    }

}