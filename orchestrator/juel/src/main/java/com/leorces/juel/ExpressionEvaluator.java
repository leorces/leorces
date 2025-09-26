package com.leorces.juel;


import java.util.Map;


/**
 * Interface for evaluating expressions in the orchestrator.
 * Similar to Camunda's expression evaluation capabilities.
 */
public interface ExpressionEvaluator {

    /**
     * Evaluates the given origin map by processing each entry with potential expressions,
     * using the data map as the variables context for evaluation.
     *
     * @param origin A map containing entries where keys and/or values might be expressions
     *               requiring evaluation.
     * @param data   A map containing variables to be used as context for expression evaluation.
     * @return The origin map with all resolvable expressions in keys and values evaluated
     * using the data context.
     */
    Map<String, Object> evaluate(Map<String, Object> origin, Map<String, Object> data);

    /**
     * Evaluates the given data map by processing each entry with potential expressions,
     * returning a new map with resolved values.
     *
     * @param data A map containing entries where keys and/or values might be expressions
     *             requiring evaluation.
     * @return A new map where all resolvable expressions in keys and values have been processed.
     */
    Map<String, Object> evaluate(Map<String, Object> data);

    /**
     * Evaluates an expression with the given variable's context.
     *
     * @param expression The expression to evaluate (e.g., "${isDepositError == true}")
     * @param variables  The variable's context for evaluation
     * @param resultType The expected result type
     * @return The result of the expression evaluation
     */
    <T> T evaluate(String expression, Map<String, Object> variables, Class<T> resultType);

    /**
     * Evaluates a boolean expression.
     *
     * @param expression The boolean expression to evaluate
     * @param variables  The variables context for evaluation
     * @return The boolean result of the expression evaluation
     */
    boolean evaluateBoolean(String expression, Map<String, Object> variables);

    /**
     * Evaluates a string expression.
     *
     * @param expression The string expression to evaluate
     * @param variables  The variables context for evaluation
     * @return The string result of the expression evaluation
     */
    String evaluateString(String expression, Map<String, Object> variables);

    /**
     * Checks if the given string is an expression (contains expression syntax).
     *
     * @param value The string to check
     * @return true if the string contains expression syntax, false otherwise
     */
    boolean isExpression(String value);

}