package com.leorces.model.definition;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Variable Mapping Tests")
class VariableMappingTest {

    private static final String TEST_SOURCE = "testSource";
    private static final String TEST_TARGET = "testTarget";
    private static final String TEST_SOURCE_EXPRESSION = "${sourceVar}";
    private static final String TEST_EXPRESSION = "${targetVar}";
    private static final String TEST_VARIABLES = "var1,var2";
    private static final String TEST_BUSINESS_KEY = "businessKey123";
    private static final boolean TEST_LOCAL = true;

    @Test
    @DisplayName("Should create VariableMapping with all fields using builder")
    void shouldCreateVariableMappingWithAllFields() {
        // When
        var mapping = VariableMapping.builder()
                .source(TEST_SOURCE)
                .target(TEST_TARGET)
                .sourceExpression(TEST_SOURCE_EXPRESSION)
                .expression(TEST_EXPRESSION)
                .variables(TEST_VARIABLES)
                .businessKey(TEST_BUSINESS_KEY)
                .local(TEST_LOCAL)
                .build();

        // Then
        assertNotNull(mapping);
        assertEquals(TEST_SOURCE, mapping.source());
        assertEquals(TEST_TARGET, mapping.target());
        assertEquals(TEST_SOURCE_EXPRESSION, mapping.sourceExpression());
        assertEquals(TEST_EXPRESSION, mapping.expression());
        assertEquals(TEST_VARIABLES, mapping.variables());
        assertEquals(TEST_BUSINESS_KEY, mapping.businessKey());
        assertEquals(TEST_LOCAL, mapping.local());
    }

    @Test
    @DisplayName("Should create VariableMapping with null fields")
    void shouldCreateVariableMappingWithNullFields() {
        // When
        var mapping = VariableMapping.builder()
                .source(null)
                .target(null)
                .sourceExpression(null)
                .expression(null)
                .variables(null)
                .businessKey(null)
                .local(false)
                .build();

        // Then
        assertNotNull(mapping);
        assertNull(mapping.source());
        assertNull(mapping.target());
        assertNull(mapping.sourceExpression());
        assertNull(mapping.expression());
        assertNull(mapping.variables());
        assertNull(mapping.businessKey());
        assertFalse(mapping.local());
    }

    @Test
    @DisplayName("Should create VariableMapping with minimal fields")
    void shouldCreateVariableMappingWithMinimalFields() {
        // When
        var mapping = VariableMapping.builder()
                .source(TEST_SOURCE)
                .local(false)
                .build();

        // Then
        assertNotNull(mapping);
        assertEquals(TEST_SOURCE, mapping.source());
        assertNull(mapping.target());
        assertNull(mapping.sourceExpression());
        assertNull(mapping.expression());
        assertNull(mapping.variables());
        assertNull(mapping.businessKey());
        assertFalse(mapping.local());
    }

    @Test
    @DisplayName("Should support toBuilder functionality")
    void shouldSupportToBuilderFunctionality() {
        // Given
        var originalMapping = VariableMapping.builder()
                .source(TEST_SOURCE)
                .target(TEST_TARGET)
                .local(false)
                .build();

        // When
        var modifiedMapping = originalMapping.toBuilder()
                .target("newTarget")
                .local(true)
                .build();

        // Then
        assertNotNull(modifiedMapping);
        assertEquals(TEST_SOURCE, modifiedMapping.source());
        assertEquals("newTarget", modifiedMapping.target());
        assertTrue(modifiedMapping.local());
        // Original should remain unchanged
        assertEquals(TEST_TARGET, originalMapping.target());
        assertFalse(originalMapping.local());
    }

    @Test
    @DisplayName("Should implement equals correctly")
    void shouldImplementEqualsCorrectly() {
        // Given
        var mapping1 = VariableMapping.builder()
                .source(TEST_SOURCE)
                .target(TEST_TARGET)
                .local(TEST_LOCAL)
                .build();

        var mapping2 = VariableMapping.builder()
                .source(TEST_SOURCE)
                .target(TEST_TARGET)
                .local(TEST_LOCAL)
                .build();

        var mapping3 = VariableMapping.builder()
                .source("differentSource")
                .target(TEST_TARGET)
                .local(TEST_LOCAL)
                .build();

        // When & Then
        assertEquals(mapping1, mapping2);
        assertNotEquals(mapping1, mapping3);
        assertNotEquals(null, mapping1);
    }

    @Test
    @DisplayName("Should implement hashCode correctly")
    void shouldImplementHashCodeCorrectly() {
        // Given
        var mapping1 = VariableMapping.builder()
                .source(TEST_SOURCE)
                .target(TEST_TARGET)
                .local(TEST_LOCAL)
                .build();

        var mapping2 = VariableMapping.builder()
                .source(TEST_SOURCE)
                .target(TEST_TARGET)
                .local(TEST_LOCAL)
                .build();

        // When & Then
        assertEquals(mapping1.hashCode(), mapping2.hashCode());
    }

    @Test
    @DisplayName("Should implement toString correctly")
    void shouldImplementToStringCorrectly() {
        // Given
        var mapping = VariableMapping.builder()
                .source(TEST_SOURCE)
                .target(TEST_TARGET)
                .local(TEST_LOCAL)
                .build();

        // When
        var toStringResult = mapping.toString();

        // Then
        assertNotNull(toStringResult);
        assertTrue(toStringResult.contains("VariableMapping"));
        assertTrue(toStringResult.contains(TEST_SOURCE));
        assertTrue(toStringResult.contains(TEST_TARGET));
        assertTrue(toStringResult.contains(String.valueOf(TEST_LOCAL)));
    }

}