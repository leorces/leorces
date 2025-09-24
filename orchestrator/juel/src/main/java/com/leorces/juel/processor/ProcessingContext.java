package com.leorces.juel.processor;


import com.leorces.juel.JuelExpressionEvaluator;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


/**
 * Encapsulates the processing context and operations for expression evaluation.
 */
public class ProcessingContext {

    private final Map<String, Object> originalData;
    private final Map<String, Object> variablesContext;
    private final JuelExpressionEvaluator evaluator;
    private final Map<Object, Object> cache;

    ProcessingContext(Map<String, Object> originalData, JuelExpressionEvaluator evaluator) {
        this.originalData = originalData;
        this.variablesContext = originalData;
        this.evaluator = evaluator;
        this.cache = new HashMap<>();
    }

    ProcessingContext(Map<String, Object> originalData, Map<String, Object> variablesContext, JuelExpressionEvaluator evaluator) {
        this.originalData = originalData;
        this.variablesContext = variablesContext;
        this.evaluator = evaluator;
        this.cache = new HashMap<>();
    }

    Map<String, Object> processData() {
        var result = new HashMap<>(originalData);
        originalData.entrySet().forEach(entry -> processEntry(entry, result));
        return result;
    }

    Map<String, Object> processDataWithExternalContext() {
        var result = new HashMap<>(originalData);
        originalData.entrySet().forEach(entry -> processEntryWithExternalContext(entry, result));
        return result;
    }

    private void processEntry(Map.Entry<String, Object> entry, Map<String, Object> result) {
        var processedValue = processValue(entry.getValue());
        if (processedValue != null) {
            result.put(entry.getKey(), processedValue);
        }
    }

    private void processEntryWithExternalContext(Map.Entry<String, Object> entry, Map<String, Object> result) {
        var processedValue = processValueWithContext(entry.getValue(), variablesContext);
        if (processedValue != null) {
            result.put(entry.getKey(), processedValue);
        }
    }

    private Object processValue(Object value) {
        if (cache.containsKey(value)) {
            return cache.get(value);
        }
        var result = processValueByType(value);
        cache.put(value, result);
        return result;
    }

    private Object processValueWithContext(Object value, Map<String, Object> context) {
        return processValueByTypeWithContext(value, context);
    }

    private Object processValueByType(Object value) {
        return switch (value) {
            case String s -> processStringValue(s);
            case Map<?, ?> map -> processMapValue(map);
            case List<?> list -> processListValue(list);
            case null, default -> value;
        };
    }

    private Object processValueByTypeWithContext(Object value, Map<String, Object> context) {
        return switch (value) {
            case String s -> processStringValueWithContext(s, context);
            case Map<?, ?> map -> processMapValue(map); // Maps create their own combined context
            case List<?> list -> processListValueWithContext(list, context);
            case null, default -> value;
        };
    }

    private String processStringValue(String value) {
        return processStringValueWithContext(value, originalData);
    }

    private String processStringValueWithContext(String value, Map<String, Object> context) {
        return evaluator.isExpression(value)
                ? evaluator.resolveVariables(value, context)
                : value;
    }

    private Map<String, Object> processMapValue(Map<?, ?> value) {
        var processedMap = new HashMap<String, Object>();

        // Create a combined context for variable resolution that includes both original data and current map
        var combinedContext = new HashMap<>(originalData);
        value.forEach((key, val) -> {
            if (key instanceof String && val != null) {
                combinedContext.put((String) key, val);
            }
        });

        value.forEach((key, value1) -> {
            var processedKey = getProcessedKey(key);
            var processedValue = processValueWithContext(value1, combinedContext);
            if (processedValue != null) {
                processedMap.put(processedKey, processedValue);
            }
        });
        return processedMap;
    }

    private String getProcessedKey(Object key) {
        var processedKey = processValue(key);
        return processedKey instanceof String ? (String) processedKey : key.toString();
    }

    private List<Object> processListValue(List<?> value) {
        return value.stream()
                .map(this::processValue)
                .collect(Collectors.toList());
    }

    private List<Object> processListValueWithContext(List<?> value, Map<String, Object> context) {
        return value.stream()
                .map(item -> processValueWithContext(item, context))
                .collect(Collectors.toList());
    }
}