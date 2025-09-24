package com.leorces.juel;


import com.leorces.juel.converter.LiteralValueConverter;
import com.leorces.juel.converter.SpelExpressionConverter;
import com.leorces.juel.exception.ExpressionEvaluationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpression;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;


@DisplayName("JuelExpressionEvaluator Tests")
@ExtendWith(MockitoExtension.class)
class JuelExpressionEvaluatorTest {

    private static final String TEST_EXPRESSION = "${username}";
    private static final String CONVERTED_EXPRESSION = "#username";
    private static final String SIMPLE_VARIABLE = "username";
    private static final String RESOLVED_VALUE = "John Doe";
    private static final Map<String, Object> TEST_VARIABLES = Map.of(
            SIMPLE_VARIABLE, RESOLVED_VALUE,
            "age", 30,
            "isActive", true
    );

    @Mock
    private ExpressionParser mockParser;
    @Mock
    private SpelExpressionConverter mockExpressionConverter;
    @Mock
    private LiteralValueConverter mockLiteralConverter;
    @Mock
    private SpelExpression mockSpelExpression;
    private JuelExpressionEvaluator evaluator;

    @BeforeEach
    void setUp() {
        evaluator = new JuelExpressionEvaluator(mockParser, mockExpressionConverter, mockLiteralConverter);
    }

    @Test
    @DisplayName("Should evaluate map with origin and data context")
    void shouldEvaluateMapWithOriginAndDataContext() {
        //Given
        var originMap = Map.<String, Object>of("key", TEST_EXPRESSION);
        when(mockExpressionConverter.convertToSpelExpression(TEST_EXPRESSION)).thenReturn(CONVERTED_EXPRESSION);
        when(mockParser.parseExpression(CONVERTED_EXPRESSION)).thenReturn(mockSpelExpression);
        when(mockSpelExpression.getValue(any(StandardEvaluationContext.class), eq(Object.class)))
                .thenReturn(RESOLVED_VALUE);

        //When
        var result = evaluator.evaluate(originMap, TEST_VARIABLES);

        //Then
        assertThat(result).isNotNull();
        assertThat(result.get("key")).isEqualTo(RESOLVED_VALUE);
        verify(mockExpressionConverter).convertToSpelExpression(TEST_EXPRESSION);
        verify(mockParser).parseExpression(CONVERTED_EXPRESSION);
    }

    @Test
    @DisplayName("Should evaluate map with self context")
    void shouldEvaluateMapWithSelfContext() {
        //Given
        var dataMap = Map.<String, Object>of("key", TEST_EXPRESSION);
        when(mockExpressionConverter.convertToSpelExpression(TEST_EXPRESSION)).thenReturn(CONVERTED_EXPRESSION);
        when(mockParser.parseExpression(CONVERTED_EXPRESSION)).thenReturn(mockSpelExpression);
        when(mockSpelExpression.getValue(any(StandardEvaluationContext.class), eq(Object.class)))
                .thenReturn(RESOLVED_VALUE);

        //When
        var result = evaluator.evaluate(dataMap);

        //Then
        assertThat(result).isNotNull();
        assertThat(result.get("key")).isEqualTo(RESOLVED_VALUE);
    }

    @Test
    @DisplayName("Should evaluate expression with specific result type")
    void shouldEvaluateExpressionWithSpecificResultType() {
        //Given
        var resultType = String.class;
        when(mockExpressionConverter.convertToSpelExpression(TEST_EXPRESSION)).thenReturn(CONVERTED_EXPRESSION);
        when(mockParser.parseExpression(CONVERTED_EXPRESSION)).thenReturn(mockSpelExpression);
        when(mockSpelExpression.getValue(any(StandardEvaluationContext.class), eq(String.class)))
                .thenReturn(RESOLVED_VALUE);

        //When
        var result = evaluator.evaluate(TEST_EXPRESSION, TEST_VARIABLES, resultType);

        //Then
        assertThat(result).isEqualTo(RESOLVED_VALUE);
        assertThat(result).isInstanceOf(String.class);
        verify(mockExpressionConverter).convertToSpelExpression(TEST_EXPRESSION);
        verify(mockSpelExpression).getValue(any(StandardEvaluationContext.class), eq(resultType));
    }

    @Test
    @DisplayName("Should evaluate boolean expression")
    void shouldEvaluateBooleanExpression() {
        //Given
        var booleanExpression = "${isActive}";
        when(mockExpressionConverter.convertToSpelExpression(booleanExpression)).thenReturn("#isActive");
        when(mockParser.parseExpression("#isActive")).thenReturn(mockSpelExpression);
        when(mockSpelExpression.getValue(any(StandardEvaluationContext.class), eq(Boolean.class)))
                .thenReturn(true);

        //When
        var result = evaluator.evaluateBoolean(booleanExpression, TEST_VARIABLES);

        //Then
        assertThat(result).isTrue();
        verify(mockSpelExpression).getValue(any(StandardEvaluationContext.class), eq(Boolean.class));
    }

    @Test
    @DisplayName("Should evaluate string expression")
    void shouldEvaluateStringExpression() {
        //Given
        when(mockExpressionConverter.convertToSpelExpression(TEST_EXPRESSION)).thenReturn(CONVERTED_EXPRESSION);
        when(mockParser.parseExpression(CONVERTED_EXPRESSION)).thenReturn(mockSpelExpression);
        when(mockSpelExpression.getValue(any(StandardEvaluationContext.class), eq(String.class)))
                .thenReturn(RESOLVED_VALUE);

        //When
        var result = evaluator.evaluateString(TEST_EXPRESSION, TEST_VARIABLES);

        //Then
        assertThat(result).isEqualTo(RESOLVED_VALUE);
        verify(mockSpelExpression).getValue(any(StandardEvaluationContext.class), eq(String.class));
    }

    @Test
    @DisplayName("Should detect expression syntax")
    void shouldDetectExpressionSyntax() {
        //Given & When & Then
        assertThat(evaluator.isExpression("${variable}")).isTrue();
        assertThat(evaluator.isExpression("{variable}")).isTrue();
        assertThat(evaluator.isExpression("plain text")).isFalse();
        assertThat(evaluator.isExpression("")).isFalse();
        assertThat(evaluator.isExpression(null)).isFalse();
    }

    @Test
    @DisplayName("Should resolve variables in text")
    void shouldResolveVariablesInText() {
        //Given
        var textWithVariables = "Hello ${username}!";
        var expectedResult = "Hello John Doe!";
        when(mockExpressionConverter.convertToSpelExpression(TEST_EXPRESSION)).thenReturn(CONVERTED_EXPRESSION);
        when(mockParser.parseExpression(CONVERTED_EXPRESSION)).thenReturn(mockSpelExpression);
        when(mockSpelExpression.getValue(any(StandardEvaluationContext.class), eq(Object.class)))
                .thenReturn(RESOLVED_VALUE);

        //When
        var result = evaluator.resolveVariables(textWithVariables, TEST_VARIABLES);

        //Then
        assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    @DisplayName("Should handle null variables in resolution")
    void shouldHandleNullVariablesInResolution() {
        //Given
        var textWithVariable = "Hello ${nullVar}!";
        var variablesWithNull = Map.<String, Object>of("nullVar", "value");
        when(mockExpressionConverter.convertToSpelExpression("${nullVar}")).thenReturn("#nullVar");
        when(mockParser.parseExpression("#nullVar")).thenReturn(mockSpelExpression);
        when(mockSpelExpression.getValue(any(StandardEvaluationContext.class), eq(Object.class)))
                .thenReturn(null);

        //When
        var result = evaluator.resolveVariables(textWithVariable, variablesWithNull);

        //Then
        assertThat(result).isEqualTo("Hello !");
    }

    @Test
    @DisplayName("Should preserve text when variable not found")
    void shouldPreserveTextWhenVariableNotFound() {
        //Given
        var textWithUnknownVar = "Hello ${unknown}!";
        when(mockExpressionConverter.convertToSpelExpression("${unknown}")).thenReturn("#unknown");
        when(mockParser.parseExpression("#unknown")).thenReturn(mockSpelExpression);
        when(mockSpelExpression.getValue(any(StandardEvaluationContext.class), eq(Object.class)))
                .thenThrow(new RuntimeException("Variable not found"));

        //When
        var result = evaluator.resolveVariables(textWithUnknownVar, TEST_VARIABLES);

        //Then
        assertThat(result).isEqualTo("Hello ${unknown}!");
    }

    @Test
    @DisplayName("Should handle exception during expression evaluation")
    void shouldHandleExceptionDuringExpressionEvaluation() {
        //Given
        var expression = "${invalid.expression}";
        when(mockExpressionConverter.convertToSpelExpression(expression)).thenReturn("#invalid.expression");
        when(mockParser.parseExpression("#invalid.expression")).thenReturn(mockSpelExpression);
        when(mockSpelExpression.getValue(any(StandardEvaluationContext.class), eq(String.class)))
                .thenThrow(new RuntimeException("Evaluation failed"));

        //When & Then
        assertThatThrownBy(() -> evaluator.evaluateString(expression, TEST_VARIABLES))
                .isInstanceOf(ExpressionEvaluationException.class)
                .hasMessageContaining("Failed to evaluate string expression")
                .hasCauseInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("Should handle exception during boolean evaluation")
    void shouldHandleExceptionDuringBooleanEvaluation() {
        //Given
        var expression = "${invalid.boolean}";
        when(mockExpressionConverter.convertToSpelExpression(expression)).thenReturn("#invalid.boolean");
        when(mockParser.parseExpression("#invalid.boolean")).thenReturn(mockSpelExpression);
        when(mockSpelExpression.getValue(any(StandardEvaluationContext.class), eq(Boolean.class)))
                .thenThrow(new RuntimeException("Boolean evaluation failed"));

        //When & Then
        assertThatThrownBy(() -> evaluator.evaluateBoolean(expression, TEST_VARIABLES))
                .isInstanceOf(ExpressionEvaluationException.class)
                .hasMessageContaining("Failed to evaluate boolean expression")
                .hasCauseInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("Should handle exception during generic evaluation")
    void shouldHandleExceptionDuringGenericEvaluation() {
        //Given
        var expression = "${invalid}";
        when(mockExpressionConverter.convertToSpelExpression(expression)).thenReturn("#invalid");
        when(mockParser.parseExpression("#invalid")).thenReturn(mockSpelExpression);
        when(mockSpelExpression.getValue(any(StandardEvaluationContext.class), eq(Object.class)))
                .thenThrow(new RuntimeException("Generic evaluation failed"));

        //When & Then
        assertThatThrownBy(() -> evaluator.evaluate(expression, TEST_VARIABLES, Object.class))
                .isInstanceOf(ExpressionEvaluationException.class)
                .hasMessageContaining("Failed to evaluate expression")
                .hasCauseInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("Should handle empty expression map")
    void shouldHandleEmptyExpressionMap() {
        //Given
        var emptyMap = Map.<String, Object>of();

        //When
        var result = evaluator.evaluate(emptyMap);

        //Then
        assertThat(result).isNotNull();
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should handle map with non-expression values")
    void shouldHandleMapWithNonExpressionValues() {
        //Given
        var mapWithPlainValues = Map.<String, Object>of(
                "plain", "value",
                "number", 42
        );

        //When
        var result = evaluator.evaluate(mapWithPlainValues);

        //Then
        assertThat(result).isNotNull();
        assertThat(result.get("plain")).isEqualTo("value");
        assertThat(result.get("number")).isEqualTo(42);
        verify(mockExpressionConverter, never()).convertToSpelExpression(anyString());
    }

    @Test
    @DisplayName("Should create evaluation context with variables")
    void shouldCreateEvaluationContextWithVariables() {
        //Given
        var variables = Map.<String, Object>of("key", "value");
        when(mockExpressionConverter.convertToSpelExpression(TEST_EXPRESSION)).thenReturn(CONVERTED_EXPRESSION);
        when(mockParser.parseExpression(CONVERTED_EXPRESSION)).thenReturn(mockSpelExpression);
        when(mockSpelExpression.getValue(any(StandardEvaluationContext.class), eq(String.class)))
                .thenReturn(RESOLVED_VALUE);

        //When
        var result = evaluator.evaluateString(TEST_EXPRESSION, variables);

        //Then
        assertThat(result).isEqualTo(RESOLVED_VALUE);
    }

    @Test
    @DisplayName("Should handle complex nested expressions")
    void shouldHandleComplexNestedExpressions() {
        //Given
        var complexMap = Map.of(
                "simple", "value",
                "expression", TEST_EXPRESSION,
                "nested", Map.of("inner", TEST_EXPRESSION)
        );
        when(mockExpressionConverter.convertToSpelExpression(TEST_EXPRESSION)).thenReturn(CONVERTED_EXPRESSION);
        when(mockParser.parseExpression(CONVERTED_EXPRESSION)).thenReturn(mockSpelExpression);
        when(mockSpelExpression.getValue(any(StandardEvaluationContext.class), eq(Object.class)))
                .thenReturn(RESOLVED_VALUE);

        //When
        var result = evaluator.evaluate(complexMap);

        //Then
        assertThat(result).isNotNull();
        assertThat(result.get("simple")).isEqualTo("value");
        assertThat(result.get("expression")).isEqualTo(RESOLVED_VALUE);
    }

    @Test
    @DisplayName("Should handle multiple expression patterns")
    void shouldHandleMultipleExpressionPatterns() {
        //Given & When & Then
        assertThat(evaluator.isExpression("${var}")).isTrue();
        assertThat(evaluator.isExpression("{var}")).isTrue();
        assertThat(evaluator.isExpression("${var.property}")).isTrue();
        assertThat(evaluator.isExpression("text ${var} more text")).isTrue();
        assertThat(evaluator.isExpression("no expressions here")).isFalse();
    }

    @Test
    @DisplayName("Should resolve multiple variables in same text")
    void shouldResolveMultipleVariablesInSameText() {
        //Given
        var textWithMultipleVars = "User: ${username}, Age: ${age}";
        var expectedText = "User: John Doe, Age: 30";

        // Mock for username
        when(mockExpressionConverter.convertToSpelExpression("${username}")).thenReturn("#username");
        when(mockParser.parseExpression("#username")).thenReturn(mockSpelExpression);

        // Mock for age
        var mockAgeExpression = org.mockito.Mockito.mock(SpelExpression.class);
        when(mockExpressionConverter.convertToSpelExpression("${age}")).thenReturn("#age");
        when(mockParser.parseExpression("#age")).thenReturn(mockAgeExpression);

        when(mockSpelExpression.getValue(any(StandardEvaluationContext.class), eq(Object.class)))
                .thenReturn(RESOLVED_VALUE);
        when(mockAgeExpression.getValue(any(StandardEvaluationContext.class), eq(Object.class)))
                .thenReturn(30);

        //When
        var result = evaluator.resolveVariables(textWithMultipleVars, TEST_VARIABLES);

        //Then
        assertThat(result).isEqualTo(expectedText);
    }

    @Test
    @DisplayName("Should handle evaluation context with root object")
    void shouldHandleEvaluationContextWithRootObject() {
        //Given
        var variables = Map.<String, Object>of("root", "rootValue");
        when(mockExpressionConverter.convertToSpelExpression(TEST_EXPRESSION)).thenReturn(CONVERTED_EXPRESSION);
        when(mockParser.parseExpression(CONVERTED_EXPRESSION)).thenReturn(mockSpelExpression);
        when(mockSpelExpression.getValue(any(StandardEvaluationContext.class), eq(Object.class)))
                .thenReturn("result");

        //When
        var result = evaluator.evaluate(TEST_EXPRESSION, variables, Object.class);

        //Then
        assertThat(result).isEqualTo("result");
        verify(mockSpelExpression).getValue(any(StandardEvaluationContext.class), eq(Object.class));
    }
}