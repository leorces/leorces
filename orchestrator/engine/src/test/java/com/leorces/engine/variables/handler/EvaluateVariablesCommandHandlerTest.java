package com.leorces.engine.variables.handler;

import com.leorces.common.mapper.VariablesMapper;
import com.leorces.engine.core.CommandDispatcher;
import com.leorces.engine.variables.command.EvaluateVariablesCommand;
import com.leorces.juel.ExpressionEvaluator;
import com.leorces.model.runtime.activity.ActivityExecution;
import com.leorces.model.runtime.variable.Variable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("EvaluateVariablesCommandHandler Tests")
class EvaluateVariablesCommandHandlerTest {

    private static final String KEY = "key";
    private static final String VALUE = "value";
    private static final String EXPRESSION = "${expr}";
    private static final String OTHER_KEY = "other";
    private static final String OTHER_VALUE = "val";
    private static final String RESOLVED_VALUE = "resolved";

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
        // Given
        var commandWithNull = EvaluateVariablesCommand.of(activity, null);
        var commandWithEmpty = EvaluateVariablesCommand.of(activity, Map.of());

        // When
        var resultNull = handler.execute(commandWithNull);
        var resultEmpty = handler.execute(commandWithEmpty);

        // Then
        assertThat(resultNull).isEmpty();
        assertThat(resultEmpty).isEmpty();
        verifyNoInteractions(expressionEvaluator, dispatcher);
    }

    @Test
    @DisplayName("Should map variables directly when no expressions are present")
    void shouldMapDirectlyWhenNoExpressions() {
        // Given
        var variables = Map.<String, Object>of(KEY, VALUE);
        var expected = List.of(mock(Variable.class));

        when(expressionEvaluator.isExpression(VALUE)).thenReturn(false);
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
        var variablesWithExpression = Map.<String, Object>of(KEY, EXPRESSION);
        var currentVariables = Map.<String, Object>of(OTHER_KEY, OTHER_VALUE);
        var evaluatedMap = Map.<String, Object>of(KEY, RESOLVED_VALUE);
        var expected = List.of(mock(Variable.class));

        when(expressionEvaluator.isExpression(EXPRESSION)).thenReturn(true);
        when(activity.getScopedVariables(any())).thenReturn(currentVariables);
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
