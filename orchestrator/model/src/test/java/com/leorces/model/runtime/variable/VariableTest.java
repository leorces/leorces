package com.leorces.model.runtime.variable;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Variable Tests")
class VariableTest {

    private static final String TEST_ID = "var-123";
    private static final String TEST_PROCESS_ID = "process-456";
    private static final String TEST_EXECUTION_ID = "execution-789";
    private static final String TEST_EXECUTION_DEFINITION_ID = "execution-def-101";
    private static final String TEST_VAR_KEY = "customerName";
    private static final String TEST_VAR_VALUE = "John Doe";
    private static final String TEST_TYPE = "String";
    private static final LocalDateTime TEST_CREATED_AT = LocalDateTime.of(2024, 1, 1, 10, 0);
    private static final LocalDateTime TEST_UPDATED_AT = LocalDateTime.of(2024, 1, 2, 10, 0);

    @Test
    @DisplayName("Should create Variable with all fields using builder")
    void shouldCreateVariableWithAllFields() {
        // When
        var variable = Variable.builder()
                .id(TEST_ID)
                .processId(TEST_PROCESS_ID)
                .executionId(TEST_EXECUTION_ID)
                .executionDefinitionId(TEST_EXECUTION_DEFINITION_ID)
                .varKey(TEST_VAR_KEY)
                .varValue(TEST_VAR_VALUE)
                .type(TEST_TYPE)
                .createdAt(TEST_CREATED_AT)
                .updatedAt(TEST_UPDATED_AT)
                .build();

        // Then
        assertNotNull(variable);
        assertEquals(TEST_ID, variable.id());
        assertEquals(TEST_PROCESS_ID, variable.processId());
        assertEquals(TEST_EXECUTION_ID, variable.executionId());
        assertEquals(TEST_EXECUTION_DEFINITION_ID, variable.executionDefinitionId());
        assertEquals(TEST_VAR_KEY, variable.varKey());
        assertEquals(TEST_VAR_VALUE, variable.varValue());
        assertEquals(TEST_TYPE, variable.type());
        assertEquals(TEST_CREATED_AT, variable.createdAt());
        assertEquals(TEST_UPDATED_AT, variable.updatedAt());
    }

    @Test
    @DisplayName("Should create Variable with null fields")
    void shouldCreateVariableWithNullFields() {
        // When
        var variable = Variable.builder()
                .id(null)
                .processId(null)
                .executionId(null)
                .executionDefinitionId(null)
                .varKey(null)
                .varValue(null)
                .type(null)
                .createdAt(null)
                .updatedAt(null)
                .build();

        // Then
        assertNotNull(variable);
        assertNull(variable.id());
        assertNull(variable.processId());
        assertNull(variable.executionId());
        assertNull(variable.executionDefinitionId());
        assertNull(variable.varKey());
        assertNull(variable.varValue());
        assertNull(variable.type());
        assertNull(variable.createdAt());
        assertNull(variable.updatedAt());
    }

    @Test
    @DisplayName("Should create Variable with minimal fields")
    void shouldCreateVariableWithMinimalFields() {
        // When
        var variable = Variable.builder()
                .id(TEST_ID)
                .varKey(TEST_VAR_KEY)
                .varValue(TEST_VAR_VALUE)
                .build();

        // Then
        assertNotNull(variable);
        assertEquals(TEST_ID, variable.id());
        assertEquals(TEST_VAR_KEY, variable.varKey());
        assertEquals(TEST_VAR_VALUE, variable.varValue());
        assertNull(variable.processId());
        assertNull(variable.executionId());
        assertNull(variable.executionDefinitionId());
        assertNull(variable.type());
        assertNull(variable.createdAt());
        assertNull(variable.updatedAt());
    }

    @Test
    @DisplayName("Should support toBuilder functionality")
    void shouldSupportToBuilderFunctionality() {
        // Given
        var originalVariable = Variable.builder()
                .id(TEST_ID)
                .varKey(TEST_VAR_KEY)
                .varValue(TEST_VAR_VALUE)
                .build();

        // When
        var modifiedVariable = originalVariable.toBuilder()
                .processId(TEST_PROCESS_ID)
                .type(TEST_TYPE)
                .updatedAt(TEST_UPDATED_AT)
                .build();

        // Then
        assertEquals(TEST_ID, modifiedVariable.id());
        assertEquals(TEST_VAR_KEY, modifiedVariable.varKey());
        assertEquals(TEST_VAR_VALUE, modifiedVariable.varValue());
        assertEquals(TEST_PROCESS_ID, modifiedVariable.processId());
        assertEquals(TEST_TYPE, modifiedVariable.type());
        assertEquals(TEST_UPDATED_AT, modifiedVariable.updatedAt());
    }

    @Test
    @DisplayName("Should implement equals correctly")
    void shouldImplementEqualsCorrectly() {
        // Given
        var variable1 = Variable.builder()
                .id(TEST_ID)
                .varKey(TEST_VAR_KEY)
                .varValue(TEST_VAR_VALUE)
                .build();

        var variable2 = Variable.builder()
                .id(TEST_ID)
                .varKey(TEST_VAR_KEY)
                .varValue(TEST_VAR_VALUE)
                .build();

        var variable3 = Variable.builder()
                .id("different-id")
                .varKey(TEST_VAR_KEY)
                .varValue(TEST_VAR_VALUE)
                .build();

        // When & Then
        assertEquals(variable1, variable2);
        assertNotEquals(variable1, variable3);
        assertNotEquals(null, variable1);
    }

    @Test
    @DisplayName("Should implement hashCode correctly")
    void shouldImplementHashCodeCorrectly() {
        // Given
        var variable1 = Variable.builder()
                .id(TEST_ID)
                .varKey(TEST_VAR_KEY)
                .varValue(TEST_VAR_VALUE)
                .build();

        var variable2 = Variable.builder()
                .id(TEST_ID)
                .varKey(TEST_VAR_KEY)
                .varValue(TEST_VAR_VALUE)
                .build();

        // When & Then
        assertEquals(variable1.hashCode(), variable2.hashCode());
    }

    @Test
    @DisplayName("Should implement toString correctly")
    void shouldImplementToStringCorrectly() {
        // Given
        var variable = Variable.builder()
                .id(TEST_ID)
                .varKey(TEST_VAR_KEY)
                .varValue(TEST_VAR_VALUE)
                .build();

        // When
        var toStringResult = variable.toString();

        // Then
        assertNotNull(toStringResult);
        assertTrue(toStringResult.contains("Variable"));
        assertTrue(toStringResult.contains(TEST_ID));
        assertTrue(toStringResult.contains(TEST_VAR_KEY));
        assertTrue(toStringResult.contains(TEST_VAR_VALUE));
    }

    @Test
    @DisplayName("Should handle different variable types")
    void shouldHandleDifferentVariableTypes() {
        // Given
        var stringVar = Variable.builder()
                .varKey("stringVar")
                .varValue("stringValue")
                .type("String")
                .build();

        var intVar = Variable.builder()
                .varKey("intVar")
                .varValue("42")
                .type("Integer")
                .build();

        // When & Then
        assertNotNull(stringVar);
        assertNotNull(intVar);
        assertEquals("String", stringVar.type());
        assertEquals("Integer", intVar.type());
        assertEquals("stringValue", stringVar.varValue());
        assertEquals("42", intVar.varValue());
    }

}