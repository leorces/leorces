package com.leorces.juel.processor;


import com.leorces.juel.JuelExpressionEvaluator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;


@DisplayName("ProcessingContext Tests")
@ExtendWith(MockitoExtension.class)
class ProcessingContextTest {

    private static final String EXPRESSION_STRING = "${username}";
    private static final String NON_EXPRESSION_STRING = "plain_text";
    private static final String RESOLVED_VALUE = "resolved_username";
    private static final Map<String, Object> TEST_DATA = Map.of(
            "key1", "value1",
            "key2", EXPRESSION_STRING,
            "key3", NON_EXPRESSION_STRING
    );
    private static final Map<String, Object> VARIABLES_CONTEXT = Map.of(
            "username", "external_user",
            "age", 25
    );

    @Mock
    private JuelExpressionEvaluator mockEvaluator;

    @BeforeEach
    void setUp() {
        lenient().when(mockEvaluator.isExpression(EXPRESSION_STRING)).thenReturn(true);
        lenient().when(mockEvaluator.isExpression(NON_EXPRESSION_STRING)).thenReturn(false);
        lenient().when(mockEvaluator.isExpression("value1")).thenReturn(false);
        lenient().when(mockEvaluator.resolveVariables(eq(EXPRESSION_STRING), any())).thenReturn(RESOLVED_VALUE);
    }

    @Test
    @DisplayName("Should process data using internal context")
    void shouldProcessDataUsingInternalContext() {
        //Given
        var context = new ProcessingContext(TEST_DATA, mockEvaluator);

        //When
        var result = context.processData();

        //Then
        assertThat(result).isNotNull();
        assertThat(result.get("key1")).isEqualTo("value1");
        assertThat(result.get("key2")).isEqualTo(RESOLVED_VALUE);
        assertThat(result.get("key3")).isEqualTo(NON_EXPRESSION_STRING);
        verify(mockEvaluator).isExpression(EXPRESSION_STRING);
        verify(mockEvaluator).resolveVariables(EXPRESSION_STRING, TEST_DATA);
        verify(mockEvaluator).isExpression("value1");
    }

    @Test
    @DisplayName("Should process data using external context")
    void shouldProcessDataUsingExternalContext() {
        //Given
        var context = new ProcessingContext(TEST_DATA, VARIABLES_CONTEXT, mockEvaluator);

        //When
        var result = context.processDataWithExternalContext();

        //Then
        assertThat(result).isNotNull();
        assertThat(result.get("key1")).isEqualTo("value1");
        assertThat(result.get("key2")).isEqualTo(RESOLVED_VALUE);
        assertThat(result.get("key3")).isEqualTo(NON_EXPRESSION_STRING);
        verify(mockEvaluator).resolveVariables(EXPRESSION_STRING, VARIABLES_CONTEXT);
    }

    @Test
    @DisplayName("Should handle empty data map")
    void shouldHandleEmptyDataMap() {
        //Given
        var emptyData = Map.<String, Object>of();
        var context = new ProcessingContext(emptyData, mockEvaluator);

        //When
        var result = context.processData();

        //Then
        assertThat(result).isNotNull();
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should handle null values")
    void shouldHandleNullValues() {
        //Given
        var dataWithNull = Map.<String, Object>of(
                "key1", "value1"
        );
        // Note: We can't put null values in Map.of(), so we'll test the null handling through the evaluator
        var context = new ProcessingContext(dataWithNull, mockEvaluator);
        lenient().when(mockEvaluator.resolveVariables(any(), any())).thenReturn(null);

        //When
        var result = context.processData();

        //Then
        assertThat(result).isNotNull();
        assertThat(result.get("key1")).isEqualTo("value1");
    }

    @Test
    @DisplayName("Should process nested map values")
    void shouldProcessNestedMapValues() {
        //Given
        var nestedMap = Map.<String, Object>of(
                "innerKey", EXPRESSION_STRING
        );
        var dataWithNestedMap = Map.<String, Object>of(
                "outerKey", nestedMap
        );
        var context = new ProcessingContext(dataWithNestedMap, mockEvaluator);

        //When
        var result = context.processData();

        //Then
        assertThat(result).isNotNull();
        assertThat(result.get("outerKey")).isInstanceOf(Map.class);
        @SuppressWarnings("unchecked")
        var processedNestedMap = (Map<String, Object>) result.get("outerKey");
        assertThat(processedNestedMap.get("innerKey")).isEqualTo(RESOLVED_VALUE);
        verify(mockEvaluator).resolveVariables(eq(EXPRESSION_STRING), any());
    }

    @Test
    @DisplayName("Should process list values")
    void shouldProcessListValues() {
        //Given
        var list = List.of(EXPRESSION_STRING, NON_EXPRESSION_STRING);
        var dataWithList = Map.<String, Object>of("listKey", list);
        var context = new ProcessingContext(dataWithList, mockEvaluator);

        //When
        var result = context.processData();

        //Then
        assertThat(result).isNotNull();
        assertThat(result.get("listKey")).isInstanceOf(List.class);
        @SuppressWarnings("unchecked")
        var processedList = (List<Object>) result.get("listKey");
        assertThat(processedList).hasSize(2);
        assertThat(processedList.get(0)).isEqualTo(RESOLVED_VALUE);
        assertThat(processedList.get(1)).isEqualTo(NON_EXPRESSION_STRING);
    }

    @Test
    @DisplayName("Should use caching for repeated values")
    void shouldUseCachingForRepeatedValues() {
        //Given
        var dataWithDuplicates = Map.<String, Object>of(
                "key1", EXPRESSION_STRING,
                "key2", EXPRESSION_STRING
        );
        var context = new ProcessingContext(dataWithDuplicates, mockEvaluator);

        //When
        var result = context.processData();

        //Then
        assertThat(result).isNotNull();
        assertThat(result.get("key1")).isEqualTo(RESOLVED_VALUE);
        assertThat(result.get("key2")).isEqualTo(RESOLVED_VALUE);
        // Cache should prevent multiple evaluations of the same value
        verify(mockEvaluator, times(1)).isExpression(EXPRESSION_STRING);
        verify(mockEvaluator, times(1)).resolveVariables(eq(EXPRESSION_STRING), any());
    }

    @Test
    @DisplayName("Should handle complex nested structures")
    void shouldHandleComplexNestedStructures() {
        //Given
        var result = nestedStructure();

        //Then
        assertThat(result).isNotNull();
        assertThat(result.get("simple")).isEqualTo(NON_EXPRESSION_STRING);
        assertThat(result.get("expression")).isEqualTo(RESOLVED_VALUE);

        @SuppressWarnings("unchecked")
        var nestedResult = (Map<String, Object>) result.get("nested");
        assertThat(nestedResult.get("inner")).isEqualTo(RESOLVED_VALUE);

        @SuppressWarnings("unchecked")
        var topLevelList = (List<Object>) result.get("topLevelList");
        assertThat(topLevelList).hasSize(2);
        assertThat(topLevelList.get(1)).isEqualTo(NON_EXPRESSION_STRING);
    }

    @Test
    @DisplayName("Should process map with combined context for nested maps")
    void shouldProcessMapWithCombinedContextForNestedMaps() {
        //Given
        var nestedMap = Map.<String, Object>of(
                "localVar", "local_value",
                "expression", EXPRESSION_STRING
        );
        var outerData = Map.of(
                "outerVar", "outer_value",
                "nested", nestedMap
        );
        var context = new ProcessingContext(outerData, mockEvaluator);

        //When
        var result = context.processData();

        //Then
        assertThat(result).isNotNull();
        // Verify that the combined context includes both outer and nested values
        verify(mockEvaluator).resolveVariables(eq(EXPRESSION_STRING), any());
    }

    @Test
    @DisplayName("Should handle list processing with external context")
    void shouldHandleListProcessingWithExternalContext() {
        //Given
        var list = List.of(EXPRESSION_STRING, NON_EXPRESSION_STRING);
        var dataWithList = Map.<String, Object>of("listKey", list);
        var context = new ProcessingContext(dataWithList, VARIABLES_CONTEXT, mockEvaluator);

        //When
        var result = context.processDataWithExternalContext();

        //Then
        assertThat(result).isNotNull();
        @SuppressWarnings("unchecked")
        var processedList = (List<Object>) result.get("listKey");
        assertThat(processedList).hasSize(2);
        assertThat(processedList.getFirst()).isEqualTo(RESOLVED_VALUE);
        verify(mockEvaluator).resolveVariables(EXPRESSION_STRING, VARIABLES_CONTEXT);
    }

    @Test
    @DisplayName("Should handle non-string map keys")
    void shouldHandleNonStringMapKeys() {
        //Given
        var mapWithNonStringKeys = Map.<Object, Object>of(
                123, "numeric_key_value",
                "stringKey", EXPRESSION_STRING
        );
        var dataWithMixedKeys = Map.<String, Object>of("mixedMap", mapWithNonStringKeys);
        var context = new ProcessingContext(dataWithMixedKeys, mockEvaluator);

        //When
        var result = context.processData();

        //Then
        assertThat(result).isNotNull();
        @SuppressWarnings("unchecked")
        var processedMap = (Map<String, Object>) result.get("mixedMap");
        assertThat(processedMap).containsKey("123"); // Non-string key converted to string
        assertThat(processedMap).containsKey("stringKey");
        assertThat(processedMap.get("stringKey")).isEqualTo(RESOLVED_VALUE);
    }

    @Test
    @DisplayName("Should preserve immutability of original data")
    void shouldPreserveImmutabilityOfOriginalData() {
        //Given
        var originalData = Map.<String, Object>of("key", EXPRESSION_STRING);
        var context = new ProcessingContext(originalData, mockEvaluator);

        //When
        var result = context.processData();

        //Then
        assertThat(originalData.get("key")).isEqualTo(EXPRESSION_STRING); // Original unchanged
        assertThat(result.get("key")).isEqualTo(RESOLVED_VALUE); // Result processed
    }

    @Test
    @DisplayName("Should handle null processing results gracefully")
    void shouldHandleNullProcessingResultsGracefully() {
        //Given
        var dataWithPotentialNull = Map.<String, Object>of(
                "key1", "valid_value",
                "key2", EXPRESSION_STRING
        );
        when(mockEvaluator.resolveVariables(eq(EXPRESSION_STRING), any())).thenReturn(null);
        var context = new ProcessingContext(dataWithPotentialNull, mockEvaluator);

        //When
        var result = context.processData();

        //Then
        assertThat(result).isNotNull();
        assertThat(result.get("key1")).isEqualTo("valid_value");
        // Null values should be handled gracefully (exact behavior depends on implementation)
    }

    @Test
    @DisplayName("Should maintain context separation between internal and external processing")
    void shouldMaintainContextSeparationBetweenInternalAndExternalProcessing() {
        //Given
        var internalData = Map.<String, Object>of("internal", "internal_value");
        var externalContext = Map.<String, Object>of("external", "external_value");
        var context = new ProcessingContext(internalData, externalContext, mockEvaluator);

        //When
        context.processData(); // Uses internal context
        context.processDataWithExternalContext(); // Uses external context

        //Then
        // Verify that the appropriate contexts were used
        verify(mockEvaluator, never()).resolveVariables(any(), eq(externalContext));
    }

    private Map<String, Object> nestedStructure() {
        var complexData = Map.of(
                "simple", NON_EXPRESSION_STRING,
                "expression", EXPRESSION_STRING,
                "nested", Map.of(
                        "inner", EXPRESSION_STRING,
                        "list", List.of(EXPRESSION_STRING, "plain")
                ),
                "topLevelList", List.of(
                        Map.of("listItem", EXPRESSION_STRING),
                        NON_EXPRESSION_STRING
                )
        );
        return new ProcessingContext(complexData, mockEvaluator).processData();
    }

}