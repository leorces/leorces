package com.leorces.juel.processor;


import com.leorces.juel.JuelExpressionEvaluator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@DisplayName("ExpressionDataProcessor Tests")
@ExtendWith(MockitoExtension.class)
class ExpressionDataProcessorTest {

    private static final Map<String, Object> TEST_DATA = Map.of(
            "key1", "value1",
            "key2", "${expression}"
    );
    private static final Map<String, Object> TEST_ORIGIN = Map.of(
            "origin1", "value1",
            "origin2", "${variable}"
    );
    private static final Map<String, Object> TEST_VARIABLES = Map.of(
            "variable", "resolved_value"
    );

    @Mock
    private JuelExpressionEvaluator mockEvaluator;
    private ExpressionDataProcessor processor;

    @BeforeEach
    void setUp() {
        processor = new ExpressionDataProcessor(mockEvaluator);
    }

    @Test
    @DisplayName("Should create processor with evaluator")
    void shouldCreateProcessorWithEvaluator() {
        //Given & When
        var processor = new ExpressionDataProcessor(mockEvaluator);

        //Then
        assertThat(processor.evaluator()).isEqualTo(mockEvaluator);
    }

    @Test
    @DisplayName("Should process data using single parameter method")
    void shouldProcessDataUsingSingleParameterMethod() {
        //Given
        when(mockEvaluator.isExpression(any())).thenReturn(true);
        when(mockEvaluator.resolveVariables(any(), any())).thenReturn("resolved_expression");

        //When
        var result = processor.process(TEST_DATA);

        //Then
        assertThat(result).isNotNull();
        assertThat(result).containsKeys("key1", "key2");
        verify(mockEvaluator, times(1)).isExpression("${expression}");
        verify(mockEvaluator, times(1)).resolveVariables("${expression}", TEST_DATA);
    }

    @Test
    @DisplayName("Should process origin with external context")
    void shouldProcessOriginWithExternalContext() {
        //Given
        when(mockEvaluator.isExpression(any())).thenReturn(true);
        when(mockEvaluator.resolveVariables(any(), any())).thenReturn("resolved_variable");

        //When
        var result = processor.process(TEST_ORIGIN, TEST_VARIABLES);

        //Then
        assertThat(result).isNotNull();
        assertThat(result).containsKeys("origin1", "origin2");
        verify(mockEvaluator, times(1)).isExpression("${variable}");
        verify(mockEvaluator, times(1)).resolveVariables("${variable}", TEST_VARIABLES);
    }

    @Test
    @DisplayName("Should handle empty data map")
    void shouldHandleEmptyDataMap() {
        //Given
        var emptyData = Map.<String, Object>of();

        //When
        var result = processor.process(emptyData);

        //Then
        assertThat(result).isNotNull();
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should handle empty origin and variables maps")
    void shouldHandleEmptyOriginAndVariablesMaps() {
        //Given
        var emptyOrigin = Map.<String, Object>of();
        var emptyVariables = Map.<String, Object>of();

        //When
        var result = processor.process(emptyOrigin, emptyVariables);

        //Then
        assertThat(result).isNotNull();
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should handle null values in data")
    void shouldHandleNullValuesInData() {
        //Given
        var dataWithNull = Map.<String, Object>of(
                "key1", "value1",
                "key2", "null_value"
        );
        when(mockEvaluator.isExpression(any())).thenReturn(false);

        //When
        var result = processor.process(dataWithNull);

        //Then
        assertThat(result).isNotNull();
        assertThat(result).containsKeys("key1", "key2");
        assertThat(result.get("key1")).isEqualTo("value1");
        assertThat(result.get("key2")).isEqualTo("null_value");
    }

    @Test
    @DisplayName("Should handle complex nested data structures")
    void shouldHandleComplexNestedDataStructures() {
        //Given
        var nestedData = Map.of(
                "simple", "value",
                "nested", Map.of("inner", "${expression}"),
                "list", java.util.List.of("${item1}", "${item2}")
        );
        when(mockEvaluator.isExpression(any())).thenReturn(true);
        when(mockEvaluator.resolveVariables(any(), any())).thenReturn("resolved");

        //When
        var result = processor.process(nestedData);

        //Then
        assertThat(result).isNotNull();
        assertThat(result).containsKeys("simple", "nested", "list");
    }

    @Test
    @DisplayName("Should delegate to ProcessingContext for single parameter")
    void shouldDelegateToProcessingContextForSingleParameter() {
        //Given
        when(mockEvaluator.isExpression("value1")).thenReturn(false);
        when(mockEvaluator.isExpression("${expression}")).thenReturn(true);
        when(mockEvaluator.resolveVariables("${expression}", TEST_DATA)).thenReturn("resolved_expression");

        //When
        var result = processor.process(TEST_DATA);

        //Then
        assertThat(result).isNotNull();
        assertThat(result.get("key1")).isEqualTo("value1");
        verify(mockEvaluator).isExpression("${expression}");
        verify(mockEvaluator).resolveVariables("${expression}", TEST_DATA);
    }

    @Test
    @DisplayName("Should delegate to ProcessingContext for dual parameters")
    void shouldDelegateToProcessingContextForDualParameters() {
        //Given
        when(mockEvaluator.isExpression("value1")).thenReturn(false);
        when(mockEvaluator.isExpression("${variable}")).thenReturn(true);
        when(mockEvaluator.resolveVariables("${variable}", TEST_VARIABLES)).thenReturn("resolved_variable");

        //When
        var result = processor.process(TEST_ORIGIN, TEST_VARIABLES);

        //Then
        assertThat(result).isNotNull();
        assertThat(result.get("origin1")).isEqualTo("value1");
        verify(mockEvaluator).isExpression("${variable}");
        verify(mockEvaluator).resolveVariables("${variable}", TEST_VARIABLES);
    }

    @Test
    @DisplayName("Should maintain immutability of input data")
    void shouldMaintainImmutabilityOfInputData() {
        //Given
        var originalData = Map.<String, Object>of("key", "${expression}");
        when(mockEvaluator.isExpression(any())).thenReturn(true);
        when(mockEvaluator.resolveVariables(any(), any())).thenReturn("resolved");

        //When
        var result = processor.process(originalData);

        //Then
        assertThat(originalData.get("key")).isEqualTo("${expression}"); // Original unchanged
        assertThat(result.get("key")).isEqualTo("resolved"); // Result processed
    }
}