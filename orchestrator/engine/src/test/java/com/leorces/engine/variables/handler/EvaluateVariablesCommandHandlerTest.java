package com.leorces.engine.variables.handler;

import com.leorces.common.mapper.VariablesMapper;
import com.leorces.engine.core.CommandDispatcher;
import com.leorces.engine.variables.command.EvaluateVariablesCommand;
import com.leorces.engine.variables.command.GetScopedVariablesCommand;
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

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("EvaluateVariablesCommandHandler Tests")
class EvaluateVariablesCommandHandlerTest {

    @Mock
    private ExpressionEvaluator expressionEvaluator;

    @Mock
    private VariablesMapper variablesMapper;

    @Mock
    private CommandDispatcher dispatcher;

    @Mock
    private ActivityExecution activity;

    @InjectMocks
    private EvaluateVariablesCommandHandler handler;

    @Test
    @DisplayName("Should return empty list for null or empty variables")
    void shouldReturnEmptyForNullOrEmpty() {
        // When
        var resultNull = handler.execute(EvaluateVariablesCommand.of(activity, null));
        var resultEmpty = handler.execute(EvaluateVariablesCommand.of(activity, Collections.emptyMap()));

        // Then
        assertThat(resultNull).isEmpty();
        assertThat(resultEmpty).isEmpty();
        verifyNoInteractions(expressionEvaluator, dispatcher);
    }

    @Test
    @DisplayName("Should map variables directly when no expressions are present")
    void shouldMapDirectlyWhenNoExpressions() {
        // Given
        var variables = Map.<String, Object>of("key", "value");
        var expected = List.of(mock(Variable.class));

        when(expressionEvaluator.isExpression("value")).thenReturn(false);
        when(variablesMapper.map(variables)).thenReturn(expected);

        var command = EvaluateVariablesCommand.of(activity, variables);

        // When
        var result = handler.execute(command);

        // Then
        assertThat(result).isEqualTo(expected);
        verify(variablesMapper).map(variables);
        verify(dispatcher, never()).execute(any());
    }

    @Test
    @DisplayName("Should evaluate expressions when present")
    void shouldEvaluateExpressions() {
        // Given
        var variablesWithExpression = Map.<String, Object>of("key", "${expr}");
        var currentVariables = Map.<String, Object>of("other", "val");
        var evaluatedMap = Map.<String, Object>of("key", "resolved");
        var expected = List.of(mock(Variable.class));

        when(expressionEvaluator.isExpression("${expr}")).thenReturn(true);
        when(dispatcher.execute(any(GetScopedVariablesCommand.class))).thenReturn(currentVariables);
        when(expressionEvaluator.evaluate(variablesWithExpression, currentVariables)).thenReturn(evaluatedMap);
        when(variablesMapper.map(evaluatedMap)).thenReturn(expected);

        var command = EvaluateVariablesCommand.of(activity, variablesWithExpression);

        // When
        var result = handler.execute(command);

        // Then
        assertThat(result).isEqualTo(expected);
        verify(expressionEvaluator).evaluate(variablesWithExpression, currentVariables);
    }

}
