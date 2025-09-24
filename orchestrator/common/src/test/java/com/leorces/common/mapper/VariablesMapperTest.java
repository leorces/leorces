package com.leorces.common.mapper;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.leorces.model.definition.ProcessDefinition;
import com.leorces.model.runtime.activity.ActivityExecution;
import com.leorces.model.runtime.process.Process;
import com.leorces.model.runtime.variable.Variable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;


@DisplayName("Variables Mapper Unit Tests")
class VariablesMapperTest {

    private static final String NULL_TYPE = "null";
    private static final String INTEGER_TYPE = "integer";
    private static final String LONG_TYPE = "long";
    private static final String BOOLEAN_TYPE = "boolean";
    private static final String DOUBLE_TYPE = "double";
    private static final String FLOAT_TYPE = "float";
    private static final String STRING_TYPE = "string";
    private static final String LIST_TYPE = "list";
    private static final String MAP_TYPE = "map";

    private VariablesMapper variablesMapper;

    @BeforeEach
    void setUp() {
        var objectMapper = new ObjectMapper();
        variablesMapper = new VariablesMapper(objectMapper);
    }

    @Test
    @DisplayName("Should return boolean type for string 'true'")
    void shouldReturnBooleanTypeForStringTrue() {
        // Given
        Map<String, Object> variables = Map.of("booleanVar", "true");

        // When
        var result = variablesMapper.map(variables);

        // Then
        assertEquals(1, result.size());
        var variable = result.getFirst();
        assertEquals("booleanVar", variable.varKey());
        assertEquals("true", variable.varValue());
        assertEquals(BOOLEAN_TYPE, variable.type());
    }

    @Test
    @DisplayName("Should return boolean type for string 'false'")
    void shouldReturnBooleanTypeForStringFalse() {
        // Given
        Map<String, Object> variables = Map.of("booleanVar", "false");

        // When
        var result = variablesMapper.map(variables);

        // Then
        assertEquals(1, result.size());
        var variable = result.getFirst();
        assertEquals("booleanVar", variable.varKey());
        assertEquals("false", variable.varValue());
        assertEquals(BOOLEAN_TYPE, variable.type());
    }

    @Test
    @DisplayName("Should return string type for other string values")
    void shouldReturnStringTypeForOtherStringValues() {
        // Given
        Map<String, Object> variables = Map.of(
                "stringVar1", "hello",
                "stringVar2", "TRUE",
                "stringVar3", "False",
                "stringVar4", " true",
                "stringVar5", "false "
        );

        // When
        var result = variablesMapper.map(variables);

        // Then
        assertEquals(5, result.size());
        result.forEach(variable -> assertEquals(STRING_TYPE, variable.type()));
    }

    @Test
    @DisplayName("Should return correct types for various data types")
    void shouldReturnCorrectTypesForVariousDataTypes() {
        // Given
        Map<String, Object> variables = new java.util.HashMap<>();
        variables.put("nullVar", null);
        variables.put("intVar", 42);
        variables.put("longVar", 42L);
        variables.put("booleanVar", true);
        variables.put("doubleVar", 3.14);
        variables.put("floatVar", 3.14f);
        variables.put("stringVar", "hello");
        variables.put("listVar", List.of(1, 2, 3));

        // When
        var result = variablesMapper.map(variables);

        // Then
        assertEquals(8, result.size());

        var nullVar = findVariableByKey(result, "nullVar");
        assertEquals(NULL_TYPE, nullVar.type());

        var intVar = findVariableByKey(result, "intVar");
        assertEquals(INTEGER_TYPE, intVar.type());

        var longVar = findVariableByKey(result, "longVar");
        assertEquals(LONG_TYPE, longVar.type());

        var booleanVar = findVariableByKey(result, "booleanVar");
        assertEquals(BOOLEAN_TYPE, booleanVar.type());

        var doubleVar = findVariableByKey(result, "doubleVar");
        assertEquals(DOUBLE_TYPE, doubleVar.type());

        var floatVar = findVariableByKey(result, "floatVar");
        assertEquals(FLOAT_TYPE, floatVar.type());

        var stringVar = findVariableByKey(result, "stringVar");
        assertEquals(STRING_TYPE, stringVar.type());

        var listVar = findVariableByKey(result, "listVar");
        assertEquals(LIST_TYPE, listVar.type());
    }

    @Test
    @DisplayName("Should convert variables back to map correctly")
    void shouldConvertVariablesBackToMapCorrectly() {
        // Given
        Map<String, Object> originalMap = Map.of(
                "booleanStringTrue", "true",
                "booleanStringFalse", "false",
                "regularString", "hello",
                "intValue", 42
        );

        // When
        var variables = variablesMapper.map(originalMap);
        var convertedMap = variablesMapper.toMap(variables);

        // Then
        assertEquals(4, convertedMap.size());
        assertEquals(true, convertedMap.get("booleanStringTrue"));
        assertEquals(false, convertedMap.get("booleanStringFalse"));
        assertEquals("hello", convertedMap.get("regularString"));
        assertEquals(42, convertedMap.get("intValue"));
    }

    @Test
    @DisplayName("Should handle empty and null inputs")
    void shouldHandleEmptyAndNullInputs() {
        // Given & When & Then
        assertTrue(variablesMapper.map(null).isEmpty());
        assertTrue(variablesMapper.map(Map.of()).isEmpty());
        assertTrue(variablesMapper.toMap(null).isEmpty());
        assertTrue(variablesMapper.toMap(List.of()).isEmpty());
    }

    @Test
    @DisplayName("Should map variable to process context")
    void shouldMapVariableToProcessContext() {
        // Given
        var processDefinition = Mockito.mock(ProcessDefinition.class);
        when(processDefinition.id()).thenReturn("proc-def-123");

        var process = Process.builder()
                .id("proc-456")
                .definition(processDefinition)
                .build();

        var variable = Variable.builder()
                .varKey("testVar")
                .varValue("testValue")
                .type(STRING_TYPE)
                .build();

        // When
        var result = variablesMapper.map(process, variable);

        // Then
        assertEquals("testVar", result.varKey());
        assertEquals("testValue", result.varValue());
        assertEquals(STRING_TYPE, result.type());
        assertEquals("proc-456", result.processId());
        assertEquals("proc-456", result.executionId());
        assertEquals("proc-def-123", result.executionDefinitionId());
    }

    @Test
    @DisplayName("Should map variable to activity execution context")
    void shouldMapVariableToActivityExecutionContext() {
        // Given
        var processDefinition = Mockito.mock(ProcessDefinition.class);
        when(processDefinition.id()).thenReturn("proc-def-123");

        var process = Process.builder()
                .id("proc-456")
                .definition(processDefinition)
                .build();

        var activityExecution = ActivityExecution.builder()
                .id("activity-789")
                .definitionId("activity-def-101")
                .process(process)
                .build();

        var variable = Variable.builder()
                .varKey("activityVar")
                .varValue("activityValue")
                .type(INTEGER_TYPE)
                .build();

        // When
        var result = variablesMapper.map(activityExecution, variable);

        // Then
        assertEquals("activityVar", result.varKey());
        assertEquals("activityValue", result.varValue());
        assertEquals(INTEGER_TYPE, result.type());
        assertEquals("proc-456", result.processId());
        assertEquals("activity-789", result.executionId());
        assertEquals("activity-def-101", result.executionDefinitionId());
    }

    @Test
    @DisplayName("Should convert variables to map with scope filtering")
    void shouldConvertVariablesToMapWithScopeFiltering() {
        // Given
        var variables = List.of(
                Variable.builder()
                        .varKey("var1")
                        .varValue("value1")
                        .type(STRING_TYPE)
                        .executionDefinitionId("scope1")
                        .build(),
                Variable.builder()
                        .varKey("var2")
                        .varValue("value2")
                        .type(STRING_TYPE)
                        .executionDefinitionId("scope2")
                        .build(),
                Variable.builder()
                        .varKey("var3")
                        .varValue("value3")
                        .type(STRING_TYPE)
                        .executionDefinitionId("scope1")
                        .build()
        );

        var scope = List.of("scope1", "scope2");

        // When
        var result = variablesMapper.toMap(variables, scope);

        // Then
        assertEquals(3, result.size());
        assertEquals("value1", result.get("var1"));
        assertEquals("value2", result.get("var2"));
        assertEquals("value3", result.get("var3"));
    }

    @Test
    @DisplayName("Should handle empty and null inputs for scoped toMap")
    void shouldHandleEmptyAndNullInputsForScopedToMap() {
        // Given & When & Then
        assertTrue(variablesMapper.toMap(null, List.of("scope1")).isEmpty());
        assertTrue(variablesMapper.toMap(List.of(), List.of("scope1")).isEmpty());
        assertTrue(variablesMapper.toMap(List.of(Variable.builder().build()), null).isEmpty());
        assertTrue(variablesMapper.toMap(List.of(Variable.builder().build()), List.of()).isEmpty());
    }

    @Test
    @DisplayName("Should not override variables with same key in scoped toMap")
    void shouldNotOverrideVariablesWithSameKeyInScopedToMap() {
        // Given
        var variables = List.of(
                Variable.builder()
                        .varKey("sameKey")
                        .varValue("firstValue")
                        .type(STRING_TYPE)
                        .executionDefinitionId("scope1")
                        .build(),
                Variable.builder()
                        .varKey("sameKey")
                        .varValue("secondValue")
                        .type(STRING_TYPE)
                        .executionDefinitionId("scope2")
                        .build()
        );

        var scope = List.of("scope1", "scope2");

        // When
        var result = variablesMapper.toMap(variables, scope);

        // Then
        assertEquals(1, result.size());
        assertEquals("firstValue", result.get("sameKey"));
    }

    @Test
    @DisplayName("Should convert string values to correct types")
    void shouldConvertStringValuesToCorrectTypes() {
        // Given & When & Then
        assertNull(variablesMapper.convertStringToValue(null, STRING_TYPE));
        assertNull(variablesMapper.convertStringToValue("any", NULL_TYPE));
        assertEquals(42, variablesMapper.convertStringToValue("42", INTEGER_TYPE));
        assertEquals(42L, variablesMapper.convertStringToValue("42", LONG_TYPE));
        assertEquals(true, variablesMapper.convertStringToValue("true", BOOLEAN_TYPE));
        assertEquals(false, variablesMapper.convertStringToValue("false", BOOLEAN_TYPE));
        assertEquals(3.14, variablesMapper.convertStringToValue("3.14", DOUBLE_TYPE));
        assertEquals(3.14f, variablesMapper.convertStringToValue("3.14", FLOAT_TYPE));
        assertEquals("hello", variablesMapper.convertStringToValue("hello", STRING_TYPE));
    }

    @Test
    @DisplayName("Should convert JSON string to list")
    void shouldConvertJsonStringToList() {
        // Given
        var jsonString = "[1,2,3]";

        // When
        var result = variablesMapper.convertStringToValue(jsonString, LIST_TYPE);

        // Then
        assertNotNull(result);
        assertInstanceOf(List.class, result);
        var list = (List<?>) result;
        assertEquals(3, list.size());
    }

    @Test
    @DisplayName("Should convert JSON string to map")
    void shouldConvertJsonStringToMap() {
        // Given
        var jsonString = "{\"key\":\"value\"}";

        // When
        var result = variablesMapper.convertStringToValue(jsonString, MAP_TYPE);

        // Then
        assertNotNull(result);
        assertInstanceOf(Map.class, result);
        var map = (Map<?, ?>) result;
        assertEquals("value", map.get("key"));
    }

    @Test
    @DisplayName("Should throw exception for invalid JSON list")
    void shouldThrowExceptionForInvalidJsonList() {
        // Given
        var invalidJson = "invalid json";

        // When & Then
        var exception = assertThrows(RuntimeException.class,
                () -> variablesMapper.convertStringToValue(invalidJson, LIST_TYPE));
        assertTrue(exception.getMessage().contains("Failed to deserialize list"));
    }

    @Test
    @DisplayName("Should throw exception for invalid JSON map")
    void shouldThrowExceptionForInvalidJsonMap() {
        // Given
        var invalidJson = "invalid json";

        // When & Then
        var exception = assertThrows(RuntimeException.class,
                () -> variablesMapper.convertStringToValue(invalidJson, MAP_TYPE));
        assertTrue(exception.getMessage().contains("Failed to deserialize map"));
    }

    @Test
    @DisplayName("Should handle default case in convertStringToValue")
    void shouldHandleDefaultCaseInConvertStringToValue() {
        // Given
        var jsonString = "{\"key\":\"value\"}";
        var unknownType = "unknown";

        // When
        var result = variablesMapper.convertStringToValue(jsonString, unknownType);

        // Then
        assertNotNull(result);
        assertInstanceOf(Map.class, result);
    }

    @Test
    @DisplayName("Should return original string for invalid JSON in default case")
    void shouldReturnOriginalStringForInvalidJsonInDefaultCase() {
        // Given
        var invalidJson = "not json";
        var unknownType = "unknown";

        // When
        var result = variablesMapper.convertStringToValue(invalidJson, unknownType);

        // Then
        assertEquals("not json", result);
    }

    @Test
    @DisplayName("Should handle map type detection and conversion")
    void shouldHandleMapTypeDetectionAndConversion() {
        // Given
        var mapValue = new LinkedHashMap<String, Object>();
        mapValue.put("key1", "value1");
        mapValue.put("key2", 42);
        var variables = new LinkedHashMap<String, Object>();
        variables.put("mapVar", mapValue);

        // When
        var result = variablesMapper.map(variables);
        var convertedMap = variablesMapper.toMap(result);

        // Then
        assertEquals(1, result.size());
        var variable = result.getFirst();
        assertEquals(MAP_TYPE, variable.type());

        var retrievedMap = (Map<?, ?>) convertedMap.get("mapVar");
        assertEquals("value1", retrievedMap.get("key1"));
        assertEquals(42, retrievedMap.get("key2"));
    }

    @Test
    @DisplayName("Should handle array type detection")
    void shouldHandleArrayTypeDetection() {
        // Given
        var arrayValue = new int[]{1, 2, 3};
        var variables = new LinkedHashMap<String, Object>();
        variables.put("arrayVar", arrayValue);

        // When
        var result = variablesMapper.map(variables);

        // Then
        assertEquals(1, result.size());
        var variable = result.getFirst();
        assertEquals(LIST_TYPE, variable.type());
    }

    @Test
    @DisplayName("Should handle complex object serialization through map conversion")
    void shouldHandleComplexObjectSerializationThroughMapConversion() {
        // Given
        var complexObject = new LinkedHashMap<String, Object>();
        complexObject.put("nested", Map.of("key", "value"));
        var variables = new LinkedHashMap<String, Object>();
        variables.put("complexVar", complexObject);

        // When
        var result = variablesMapper.map(variables);
        var convertedBack = variablesMapper.toMap(result);

        // Then
        assertEquals(1, result.size());
        var variable = result.getFirst();
        assertEquals(MAP_TYPE, variable.type());
        assertNotNull(convertedBack.get("complexVar"));
    }

    private Variable findVariableByKey(List<Variable> variables, String key) {
        return variables.stream()
                .filter(v -> key.equals(v.varKey()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Variable with key " + key + " not found"));
    }
}