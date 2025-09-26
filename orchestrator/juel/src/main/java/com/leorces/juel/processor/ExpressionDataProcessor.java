package com.leorces.juel.processor;


import com.leorces.juel.JuelExpressionEvaluator;

import java.util.Map;


/**
 * Handles iterative processing of complex data structures with expressions.
 */
public record ExpressionDataProcessor(JuelExpressionEvaluator evaluator) {

    /**
     * Processes the given data map by evaluating expressions.
     *
     * @param data the data map to process
     * @return processed data map with evaluated expressions
     */
    public Map<String, Object> process(Map<String, Object> data) {
        var context = new ProcessingContext(data, evaluator);
        return context.processData();
    }

    /**
     * Processes the given origin map by evaluating expressions using the data map as variables context.
     *
     * @param origin the origin map to process
     * @param data   the data map to use as variables context for expression evaluation
     * @return processed origin map with evaluated expressions
     */
    public Map<String, Object> process(Map<String, Object> origin, Map<String, Object> data) {
        var context = new ProcessingContext(origin, data, evaluator);
        return context.processDataWithExternalContext();
    }

}