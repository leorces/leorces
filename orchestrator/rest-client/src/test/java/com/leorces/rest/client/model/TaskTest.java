package com.leorces.rest.client.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.leorces.common.mapper.VariablesMapper;
import com.leorces.model.runtime.process.ProcessState;
import com.leorces.model.runtime.variable.Variable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Task Model Tests")
class TaskTest {

    private static final String STRING_TYPE = "string";
    private static final String INTEGER_TYPE = "integer";
    private static final String LONG_TYPE = "long";
    private static final String BOOLEAN_TYPE = "boolean";
    private static final String DOUBLE_TYPE = "double";
    private static final String FLOAT_TYPE = "float";
    private static final String LIST_TYPE = "list";
    private static final String MAP_TYPE = "map";
    private static final String NULL_TYPE = "null";
    private static final String TASK_ID = "task-123";
    private static final String BUSINESS_KEY = "business-key-456";
    private static final String DEFINITION_ID = "definition-789";

    private ObjectMapper objectMapper;
    private VariablesMapper variablesMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        variablesMapper = new VariablesMapper(objectMapper);
    }

    @Test
    @DisplayName("Should return string value when variable exists")
    void shouldReturnStringValueWhenVariableExists() {
        // Given
        var stringVariable = createVariable("stringVar", "hello world", STRING_TYPE);
        var task = createTaskWithVariables(List.of(stringVariable));

        // When
        var result = task.getVariable("stringVar");

        // Then
        assertEquals("hello world", result);
    }

    @Test
    @DisplayName("Should return integer value when variable exists")
    void shouldReturnIntegerValueWhenVariableExists() {
        // Given
        var integerVariable = createVariable("intVar", "42", INTEGER_TYPE);
        var task = createTaskWithVariables(List.of(integerVariable));

        // When
        var result = task.getVariable("intVar");

        // Then
        assertEquals(42, result);
    }

    @Test
    @DisplayName("Should return long value when variable exists")
    void shouldReturnLongValueWhenVariableExists() {
        // Given
        var longVariable = createVariable("longVar", "123456789", LONG_TYPE);
        var task = createTaskWithVariables(List.of(longVariable));

        // When
        var result = task.getVariable("longVar");

        // Then
        assertEquals(123456789L, result);
    }

    @Test
    @DisplayName("Should return boolean value when variable exists")
    void shouldReturnBooleanValueWhenVariableExists() {
        // Given
        var booleanVariable = createVariable("boolVar", "true", BOOLEAN_TYPE);
        var task = createTaskWithVariables(List.of(booleanVariable));

        // When
        var result = task.getVariable("boolVar");

        // Then
        assertEquals(true, result);
    }

    @Test
    @DisplayName("Should return double value when variable exists")
    void shouldReturnDoubleValueWhenVariableExists() {
        // Given
        var doubleVariable = createVariable("doubleVar", "3.14", DOUBLE_TYPE);
        var task = createTaskWithVariables(List.of(doubleVariable));

        // When
        var result = task.getVariable("doubleVar");

        // Then
        assertEquals(3.14, result);
    }

    @Test
    @DisplayName("Should return float value when variable exists")
    void shouldReturnFloatValueWhenVariableExists() {
        // Given
        var floatVariable = createVariable("floatVar", "2.5", FLOAT_TYPE);
        var task = createTaskWithVariables(List.of(floatVariable));

        // When
        var result = task.getVariable("floatVar");

        // Then
        assertEquals(2.5f, result);
    }

    @Test
    @DisplayName("Should return list value when variable exists")
    void shouldReturnListValueWhenVariableExists() {
        // Given
        var listVariable = createVariable("listVar", "[1,2,3]", LIST_TYPE);
        var task = createTaskWithVariables(List.of(listVariable));

        // When
        List<Integer> result = task.getVariable("listVar");

        // Then
        assertEquals(3, result.size());
        assertEquals(1, result.get(0));
        assertEquals(2, result.get(1));
        assertEquals(3, result.get(2));
    }

    @Test
    @DisplayName("Should return map value when variable exists")
    void shouldReturnMapValueWhenVariableExists() {
        // Given
        var mapVariable = createVariable("mapVar", "{\"key1\":\"value1\",\"key2\":42}", MAP_TYPE);
        var task = createTaskWithVariables(List.of(mapVariable));

        // When
        Map<String, Object> result = task.getVariable("mapVar");

        // Then
        assertEquals(2, result.size());
        assertEquals("value1", result.get("key1"));
        assertEquals(42, result.get("key2"));
    }

    @Test
    @DisplayName("Should return null when variable type is null")
    void shouldReturnNullWhenVariableTypeIsNull() {
        // Given
        var nullVariable = createVariable("nullVar", "someValue", NULL_TYPE);
        var task = createTaskWithVariables(List.of(nullVariable));

        // When
        var result = task.getVariable("nullVar");

        // Then
        assertNull(result);
    }

    @Test
    @DisplayName("Should return null when variable value is null")
    void shouldReturnNullWhenVariableValueIsNull() {
        // Given
        var nullValueVariable = createVariable("nullValueVar", null, STRING_TYPE);
        var task = createTaskWithVariables(List.of(nullValueVariable));

        // When
        var result = task.getVariable("nullValueVar");

        // Then
        assertNull(result);
    }

    @Test
    @DisplayName("Should return null when variable does not exist")
    void shouldReturnNullWhenVariableDoesNotExist() {
        // Given
        var existingVariable = createVariable("existingVar", "value", STRING_TYPE);
        var task = createTaskWithVariables(List.of(existingVariable));

        // When
        var result = task.getVariable("nonExistentVar");

        // Then
        assertNull(result);
    }

    @Test
    @DisplayName("Should return null when variables list is null")
    void shouldReturnNullWhenVariablesListIsNull() {
        // Given
        var task = createTaskWithVariables(null);

        // When
        var result = task.getVariable("anyVar");

        // Then
        assertNull(result);
    }

    @Test
    @DisplayName("Should return null when variables list is empty")
    void shouldReturnNullWhenVariablesListIsEmpty() {
        // Given
        var task = createTaskWithVariables(List.of());

        // When
        var result = task.getVariable("anyVar");

        // Then
        assertNull(result);
    }

    @Test
    @DisplayName("Should return null when variable name is null")
    void shouldReturnNullWhenVariableNameIsNull() {
        // Given
        var variable = createVariable("validVar", "value", STRING_TYPE);
        var task = createTaskWithVariables(List.of(variable));

        // When
        var result = task.getVariable(null);

        // Then
        assertNull(result);
    }

    @Test
    @DisplayName("Should return null when variable name is empty string")
    void shouldReturnNullWhenVariableNameIsEmptyString() {
        // Given
        var variable = createVariable("validVar", "value", STRING_TYPE);
        var task = createTaskWithVariables(List.of(variable));

        // When
        var result = task.getVariable("");

        // Then
        assertNull(result);
    }

    @Test
    @DisplayName("Should return correct variable when multiple variables exist")
    void shouldReturnCorrectVariableWhenMultipleVariablesExist() {
        // Given
        var stringVar = createVariable("stringVar", "hello", STRING_TYPE);
        var intVar = createVariable("intVar", "42", INTEGER_TYPE);
        var boolVar = createVariable("boolVar", "true", BOOLEAN_TYPE);
        var task = createTaskWithVariables(List.of(stringVar, intVar, boolVar));

        // When & Then
        var stringResult = task.getVariable("stringVar");
        var intResult = task.getVariable("intVar");
        var boolResult = task.getVariable("boolVar");

        assertEquals("hello", stringResult);
        assertEquals(42, intResult);
        assertEquals(true, boolResult);
    }

    @Test
    @DisplayName("Should handle complex JSON objects correctly")
    void shouldHandleComplexJsonObjectsCorrectly() {
        // Given
        var complexJson = "{\"user\":{\"name\":\"John\",\"age\":30,\"active\":true},\"items\":[\"item1\",\"item2\"]}";
        var complexVariable = createVariable("complexVar", complexJson, MAP_TYPE);
        var task = createTaskWithVariables(List.of(complexVariable));

        // When
        Map<String, Object> result = task.getVariable("complexVar");

        // Then
        assertTrue(result.containsKey("user"));
        assertTrue(result.containsKey("items"));

        var user = (Map<String, Object>) result.get("user");
        assertEquals("John", user.get("name"));
        assertEquals(30, user.get("age"));
        assertEquals(true, user.get("active"));

        var items = (List<String>) result.get("items");
        assertEquals(2, items.size());
        assertEquals("item1", items.get(0));
        assertEquals("item2", items.get(1));
    }

    @Test
    @DisplayName("Should deserialize custom object when using getVariable with Class parameter")
    void shouldDeserializeCustomObjectWhenUsingGetVariableWithClassParameter() {
        // Given
        var userJson = "{\"name\":\"John Doe\",\"age\":25,\"active\":true}";
        var userVariable = createVariable("user", userJson, MAP_TYPE);
        var task = createTaskWithVariables(List.of(userVariable));

        // When
        var result = task.getVariable(User.class, "user");

        // Then
        assertEquals("John Doe", result.name());
        assertEquals(25, result.age());
        assertEquals(true, result.active());
    }

    @Test
    @DisplayName("Should return null when custom object variable does not exist")
    void shouldReturnNullWhenCustomObjectVariableDoesNotExist() {
        // Given
        var existingVariable = createVariable("existingVar", "value", STRING_TYPE);
        var task = createTaskWithVariables(List.of(existingVariable));

        // When
        var result = task.getVariable(User.class, "nonExistentUser");

        // Then
        assertNull(result);
    }

    @Test
    @DisplayName("Should return null when custom object variable value is null")
    void shouldReturnNullWhenCustomObjectVariableValueIsNull() {
        // Given
        var nullValueVariable = createVariable("user", null, MAP_TYPE);
        var task = createTaskWithVariables(List.of(nullValueVariable));

        // When
        User result = task.getVariable(User.class, "user");

        // Then
        assertNull(result);
    }

    private Variable createVariable(String key, String value, String type) {
        var now = LocalDateTime.now();
        return Variable.builder()
                .id("var-id-" + key)
                .processId("process-123")
                .executionId("execution-123")
                .executionDefinitionId("execution-def-123")
                .varKey(key)
                .varValue(value)
                .type(type)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    private Task createTaskWithVariables(List<Variable> variables) {
        return Task.builder()
                .id(TASK_ID)
                .businessKey(BUSINESS_KEY)
                .definitionId(DEFINITION_ID)
                .state(ProcessState.ACTIVE)
                .retries(0)
                .variables(variables)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .startedAt(null)
                .completedAt(null)
                .objectMapper(objectMapper)
                .variablesMapper(variablesMapper)
                .build();
    }
}