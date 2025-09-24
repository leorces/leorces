package com.leorces.common.mapper;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.leorces.model.runtime.activity.ActivityExecution;
import com.leorces.model.runtime.process.Process;
import com.leorces.model.runtime.variable.Variable;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


/**
 * Mapper for variable transformations between different formats and contexts.
 * Handles conversion between Variable objects, Maps, and various data types with proper type detection.
 */
@Component
@AllArgsConstructor
public class VariablesMapper {

    private static final String NULL_TYPE = "null";
    private static final String INTEGER_TYPE = "integer";
    private static final String LONG_TYPE = "long";
    private static final String BOOLEAN_TYPE = "boolean";
    private static final String DOUBLE_TYPE = "double";
    private static final String FLOAT_TYPE = "float";
    private static final String STRING_TYPE = "string";
    private static final String LIST_TYPE = "list";
    private static final String MAP_TYPE = "map";
    private final ObjectMapper objectMapper;


    /**
     * Maps a variable to a process context by setting process-specific identifiers.
     *
     * @param process  the process to associate with the variable
     * @param variable the variable to map
     * @return a new Variable instance associated with the process
     */
    public Variable map(Process process, Variable variable) {
        return variable.toBuilder()
                .processId(process.id())
                .executionId(process.id())
                .executionDefinitionId(process.definitionId())
                .build();
    }

    /**
     * Maps a variable to an activity execution context by setting activity-specific identifiers.
     *
     * @param activity the activity execution to associate with the variable
     * @param variable the variable to map
     * @return a new Variable instance associated with the activity execution
     */
    public Variable map(ActivityExecution activity, Variable variable) {
        return variable.toBuilder()
                .processId(activity.processId())
                .executionId(activity.id())
                .executionDefinitionId(activity.definitionId())
                .build();
    }

    /**
     * Converts a map of variables to a list of Variable objects with proper type detection.
     *
     * @param variablesMap the map of variables to convert
     * @return a list of Variable objects, or empty list if input is null or empty
     */
    public List<Variable> map(Map<String, Object> variablesMap) {
        if (variablesMap == null || variablesMap.isEmpty()) {
            return Collections.emptyList();
        }

        return variablesMap.entrySet().stream()
                .map(entry -> Variable.builder()
                        .varKey(entry.getKey())
                        .varValue(convertValueToString(entry.getValue()))
                        .type(getVariableType(entry.getValue()))
                        .build()
                )
                .toList();
    }

    /**
     * Converts a list of Variable objects back to a map of key-value pairs.
     *
     * @param variables the list of variables to convert
     * @return a map of variable keys to their converted values, or empty map if input is null or empty
     */
    public Map<String, Object> toMap(List<Variable> variables) {
        if (variables == null || variables.isEmpty()) {
            return Collections.emptyMap();
        }

        var result = new LinkedHashMap<String, Object>();
        variables.forEach(v -> result.put(v.varKey(), convertStringToValue(v.varValue(), v.type())));
        return result;
    }

    /**
     * Converts a list of Variable objects to a map, filtering by execution definition scope.
     * Variables are processed in the order of the scope list, with later scopes overriding earlier ones.
     *
     * @param variables the list of variables to convert
     * @param scope     the list of execution definition IDs to filter by, in priority order
     * @return a map of variable keys to their converted values, or empty map if inputs are null or empty
     */
    public Map<String, Object> toMap(List<Variable> variables, List<String> scope) {
        if (variables == null || variables.isEmpty() || scope == null || scope.isEmpty()) {
            return Collections.emptyMap();
        }

        var result = new LinkedHashMap<String, Object>();
        for (String sc : scope) {
            variables.stream()
                    .filter(v -> sc.equals(v.executionDefinitionId()))
                    .filter(v -> !result.containsKey(v.varKey()))
                    .forEach(v -> result.put(v.varKey(), convertStringToValue(v.varValue(), v.type())));
        }
        return result;
    }

    /**
     * Converts a string value back to its original type based on the type identifier.
     * Supports primitive types, collections, and complex objects with JSON deserialization.
     *
     * @param value the string representation of the value
     * @param type  the type identifier indicating how to convert the value
     * @return the converted value in its original type, or null if value is null
     * @throws RuntimeException if deserialization of complex types fails
     */
    public Object convertStringToValue(String value, String type) {
        if (value == null) {
            return null;
        }

        return switch (type) {
            case NULL_TYPE -> null;
            case INTEGER_TYPE -> Integer.valueOf(value);
            case LONG_TYPE -> Long.valueOf(value);
            case BOOLEAN_TYPE -> Boolean.valueOf(value);
            case DOUBLE_TYPE -> Double.valueOf(value);
            case FLOAT_TYPE -> Float.valueOf(value);
            case STRING_TYPE -> value;
            case LIST_TYPE -> {
                try {
                    yield objectMapper.readValue(value, List.class);
                } catch (Exception e) {
                    throw new RuntimeException("Failed to deserialize list from value: " + value, e);
                }
            }
            case MAP_TYPE -> {
                try {
                    yield objectMapper.readValue(value, Map.class);
                } catch (Exception e) {
                    throw new RuntimeException("Failed to deserialize map from value: " + value, e);
                }
            }
            default -> {
                try {
                    yield objectMapper.readValue(value, Map.class);
                } catch (Exception e) {
                    yield value;
                }
            }
        };
    }

    private String getVariableType(Object value) {
        return switch (value) {
            case null -> NULL_TYPE;
            case Integer ignored -> INTEGER_TYPE;
            case Long ignored -> LONG_TYPE;
            case Boolean ignored -> BOOLEAN_TYPE;
            case Double ignored -> DOUBLE_TYPE;
            case Float ignored -> FLOAT_TYPE;
            case String s when "true".equals(s) || "false".equals(s) -> BOOLEAN_TYPE;
            case String ignored -> STRING_TYPE;
            case List<?> ignored -> LIST_TYPE;
            case Object obj when obj.getClass().isArray() -> LIST_TYPE;
            default -> MAP_TYPE;
        };
    }

    private String convertValueToString(Object value) {
        if (value == null) {
            return null;
        }

        return switch (value) {
            case String s -> s;
            case Integer i -> String.valueOf(i);
            case Long l -> String.valueOf(l);
            case Boolean b -> String.valueOf(b);
            case Double d -> String.valueOf(d);
            case Float f -> String.valueOf(f);
            default -> {
                try {
                    yield objectMapper.writeValueAsString(value);
                } catch (Exception e) {
                    throw new RuntimeException("Failed to serialize variable value to JSON string", e);
                }
            }
        };
    }

}
