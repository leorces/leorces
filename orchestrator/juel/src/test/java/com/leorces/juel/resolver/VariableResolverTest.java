package com.leorces.juel.resolver;


import com.leorces.juel.ExpressionEvaluator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;


@DisplayName("VariableResolver Tests")
@ExtendWith(MockitoExtension.class)
class VariableResolverTest {

    private static final String SIMPLE_VARIABLE_NAME = "username";
    private static final String RESOLVED_VALUE = "John Doe";
    private static final Map<String, Object> TEST_VARIABLES = Map.of(
            SIMPLE_VARIABLE_NAME, RESOLVED_VALUE,
            "age", 30,
            "isActive", true
    );

    @Mock
    private ExpressionEvaluator mockExpressionEvaluator;
    private VariableResolver variableResolver;

    @BeforeEach
    void setUp() {
        variableResolver = new VariableResolver(mockExpressionEvaluator);
    }

    @Test
    @DisplayName("Should resolve dollar brace expression")
    void shouldResolveDollarBraceExpression() {
        //Given
        var text = "Hello ${username}!";
        when(mockExpressionEvaluator.evaluate("${username}", TEST_VARIABLES, Object.class))
                .thenReturn(RESOLVED_VALUE);

        //When
        var result = variableResolver.resolveVariables(text, TEST_VARIABLES);

        //Then
        assertThat(result).isEqualTo("Hello John Doe!");
        verify(mockExpressionEvaluator).evaluate("${username}", TEST_VARIABLES, Object.class);
    }

    @Test
    @DisplayName("Should resolve simple brace expression")
    void shouldResolveSimpleBraceExpression() {
        //Given
        var text = "Hello {username}!";
        when(mockExpressionEvaluator.evaluate("${username}", TEST_VARIABLES, Object.class))
                .thenReturn(RESOLVED_VALUE);

        //When
        var result = variableResolver.resolveVariables(text, TEST_VARIABLES);

        //Then
        assertThat(result).isEqualTo("Hello John Doe!");
        verify(mockExpressionEvaluator).evaluate("${username}", TEST_VARIABLES, Object.class);
    }

    @Test
    @DisplayName("Should resolve multiple expressions in same text")
    void shouldResolveMultipleExpressionsInSameText() {
        //Given
        var text = "User: ${username}, Age: ${age}";
        when(mockExpressionEvaluator.evaluate("${username}", TEST_VARIABLES, Object.class))
                .thenReturn(RESOLVED_VALUE);
        when(mockExpressionEvaluator.evaluate("${age}", TEST_VARIABLES, Object.class))
                .thenReturn(30);

        //When
        var result = variableResolver.resolveVariables(text, TEST_VARIABLES);

        //Then
        assertThat(result).isEqualTo("User: John Doe, Age: 30");
        verify(mockExpressionEvaluator).evaluate("${username}", TEST_VARIABLES, Object.class);
        verify(mockExpressionEvaluator).evaluate("${age}", TEST_VARIABLES, Object.class);
    }

    @Test
    @DisplayName("Should resolve mixed expression types")
    void shouldResolveMixedExpressionTypes() {
        //Given
        var text = "User: ${username}, Status: {isActive}";
        when(mockExpressionEvaluator.evaluate("${username}", TEST_VARIABLES, Object.class))
                .thenReturn(RESOLVED_VALUE);
        when(mockExpressionEvaluator.evaluate("${isActive}", TEST_VARIABLES, Object.class))
                .thenReturn(true);

        //When
        var result = variableResolver.resolveVariables(text, TEST_VARIABLES);

        //Then
        assertThat(result).isEqualTo("User: John Doe, Status: true");
        verify(mockExpressionEvaluator).evaluate("${username}", TEST_VARIABLES, Object.class);
        verify(mockExpressionEvaluator).evaluate("${isActive}", TEST_VARIABLES, Object.class);
    }

    @Test
    @DisplayName("Should return original text when no expressions found")
    void shouldReturnOriginalTextWhenNoExpressionsFound() {
        //Given
        var text = "Hello World!";

        //When
        var result = variableResolver.resolveVariables(text, TEST_VARIABLES);

        //Then
        assertThat(result).isEqualTo("Hello World!");
        verify(mockExpressionEvaluator, never()).evaluate(any(), any(), any());
    }

    @Test
    @DisplayName("Should handle null evaluation result")
    void shouldHandleNullEvaluationResult() {
        //Given
        var text = "Hello ${username}!";
        var variables = new HashMap<String, Object>();
        variables.put(SIMPLE_VARIABLE_NAME, null);
        when(mockExpressionEvaluator.evaluate("${username}", variables, Object.class))
                .thenReturn(null);

        //When
        var result = variableResolver.resolveVariables(text, variables);

        //Then
        assertThat(result).isEqualTo("Hello !");
        verify(mockExpressionEvaluator).evaluate("${username}", variables, Object.class);
    }

    @Test
    @DisplayName("Should preserve expression when variable not found")
    void shouldPreserveExpressionWhenVariableNotFound() {
        //Given
        var text = "Hello ${nonexistent}!";
        var emptyVariables = Map.<String, Object>of();
        when(mockExpressionEvaluator.evaluate("${nonexistent}", emptyVariables, Object.class))
                .thenThrow(new RuntimeException("Variable not found"));

        //When
        var result = variableResolver.resolveVariables(text, emptyVariables);

        //Then
        assertThat(result).isEqualTo("Hello ${nonexistent}!");
        verify(mockExpressionEvaluator, times(2)).evaluate("${nonexistent}", emptyVariables, Object.class);
    }

    @Test
    @DisplayName("Should handle evaluation exception and preserve expression")
    void shouldHandleEvaluationExceptionAndPreserveExpression() {
        //Given
        var text = "Result: ${complex.expression}";
        when(mockExpressionEvaluator.evaluate("${complex.expression}", TEST_VARIABLES, Object.class))
                .thenThrow(new RuntimeException("Evaluation failed"));

        //When
        var result = variableResolver.resolveVariables(text, TEST_VARIABLES);

        //Then
        assertThat(result).isEqualTo("Result: ${complex.expression}");
        verify(mockExpressionEvaluator, times(2)).evaluate("${complex.expression}", TEST_VARIABLES, Object.class);
    }

    @Test
    @DisplayName("Should handle empty string input")
    void shouldHandleEmptyStringInput() {
        //Given
        var text = "";

        //When
        var result = variableResolver.resolveVariables(text, TEST_VARIABLES);

        //Then
        assertThat(result).isEqualTo("");
        verify(mockExpressionEvaluator, never()).evaluate(any(), any(), any());
    }

    @Test
    @DisplayName("Should handle null text input")
    void shouldHandleNullTextInput() {
        //When
        var result = variableResolver.resolveVariables(null, TEST_VARIABLES);

        //Then
        assertThat(result).isNull();
        verify(mockExpressionEvaluator, never()).evaluate(any(), any(), any());
    }

    @Test
    @DisplayName("Should handle empty variables map")
    void shouldHandleEmptyVariablesMap() {
        //Given
        var text = "Hello ${username}!";
        var emptyVariables = Map.<String, Object>of();
        when(mockExpressionEvaluator.evaluate("${username}", emptyVariables, Object.class))
                .thenThrow(new RuntimeException("Variable not found"));

        //When
        var result = variableResolver.resolveVariables(text, emptyVariables);

        //Then
        assertThat(result).isEqualTo("Hello ${username}!");
        verify(mockExpressionEvaluator, times(2)).evaluate("${username}", emptyVariables, Object.class);
    }

    @Test
    @DisplayName("Should handle complex expressions with dots and method calls")
    void shouldHandleComplexExpressionsWithDotsAndMethodCalls() {
        //Given
        var text = "Value: ${user.getName()}";
        when(mockExpressionEvaluator.evaluate("${user.getName()}", TEST_VARIABLES, Object.class))
                .thenReturn("Complex Result");

        //When
        var result = variableResolver.resolveVariables(text, TEST_VARIABLES);

        //Then
        assertThat(result).isEqualTo("Value: Complex Result");
        verify(mockExpressionEvaluator).evaluate("${user.getName()}", TEST_VARIABLES, Object.class);
    }

    @Test
    @DisplayName("Should handle nested braces correctly")
    void shouldHandleNestedBracesCorrectly() {
        //Given
        var text = "Config: ${config.getValue('key')}";
        when(mockExpressionEvaluator.evaluate("${config.getValue('key')}", TEST_VARIABLES, Object.class))
                .thenReturn("nested_value");

        //When
        var result = variableResolver.resolveVariables(text, TEST_VARIABLES);

        //Then
        assertThat(result).isEqualTo("Config: nested_value");
        verify(mockExpressionEvaluator).evaluate("${config.getValue('key')}", TEST_VARIABLES, Object.class);
    }

    @Test
    @DisplayName("Should preserve original expression format in error cases")
    void shouldPreserveOriginalExpressionFormatInErrorCases() {
        //Given
        var text = "Dollar: ${error1}, Brace: {error2}";
        when(mockExpressionEvaluator.evaluate(any(), any(), any()))
                .thenThrow(new RuntimeException("Evaluation failed"));

        //When
        var result = variableResolver.resolveVariables(text, TEST_VARIABLES);

        //Then
        assertThat(result).isEqualTo("Dollar: ${error1}, Brace: {error2}");
    }

    @Test
    @DisplayName("Should handle boolean and numeric results correctly")
    void shouldHandleBooleanAndNumericResultsCorrectly() {
        //Given
        var text = "Active: ${isActive}, Count: ${age}";
        when(mockExpressionEvaluator.evaluate("${isActive}", TEST_VARIABLES, Object.class))
                .thenReturn(true);
        when(mockExpressionEvaluator.evaluate("${age}", TEST_VARIABLES, Object.class))
                .thenReturn(30);

        //When
        var result = variableResolver.resolveVariables(text, TEST_VARIABLES);

        //Then
        assertThat(result).isEqualTo("Active: true, Count: 30");
    }

    @Test
    @DisplayName("Should handle special characters in variable names")
    void shouldHandleSpecialCharactersInVariableNames() {
        //Given
        var text = "Value: ${user_name}, ID: ${user123}";
        var variables = Map.<String, Object>of("user_name", "John", "user123", "ID123");
        when(mockExpressionEvaluator.evaluate("${user_name}", variables, Object.class))
                .thenReturn("John");
        when(mockExpressionEvaluator.evaluate("${user123}", variables, Object.class))
                .thenReturn("ID123");

        //When
        var result = variableResolver.resolveVariables(text, variables);

        //Then
        assertThat(result).isEqualTo("Value: John, ID: ID123");
    }

}