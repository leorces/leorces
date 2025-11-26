package com.leorces.juel;


import com.leorces.juel.converter.LiteralValueConverter;
import com.leorces.juel.converter.SpelExpressionConverter;
import com.leorces.juel.exception.ExpressionEvaluationException;
import com.leorces.juel.processor.ExpressionDataProcessor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.regex.Pattern;


/**
 * Spring Expression Language (SpEL) implementation of ExpressionEvaluator.
 * Replaces the JUEL-based implementation with SpEL for better Spring integration.
 */
@Slf4j
@Component
public class JuelExpressionEvaluator implements ExpressionEvaluator {

    private static final Pattern DOLLAR_PATTERN = Pattern.compile("\\$\\{[^}]+}");
    private static final Pattern HASH_PATTERN = Pattern.compile("#\\{[^}]+}");
    private static final Pattern BRACE_PATTERN = Pattern.compile("\\{[^}]+}");
    private static final Pattern DOLLAR_CAPTURE_PATTERN = Pattern.compile("\\$\\{([^}]+)}");
    private static final String EMPTY_STRING = "";

    private final ExpressionParser parser;
    private final SpelExpressionConverter expressionConverter;
    private final ExpressionDataProcessor dataProcessor;
    private final LiteralValueConverter literalValueConverter;


    public JuelExpressionEvaluator(ExpressionParser parser,
                                   SpelExpressionConverter expressionConverter,
                                   LiteralValueConverter literalValueConverter) {
        this.parser = parser;
        this.literalValueConverter = literalValueConverter;
        this.expressionConverter = expressionConverter;
        this.dataProcessor = new ExpressionDataProcessor(this);
    }


    /**
     * Evaluates the given origin map by processing each entry with potential expressions,
     * using the data map as the variables context for evaluation.
     */
    @Override
    public Map<String, Object> evaluate(Map<String, Object> origin, Map<String, Object> data) {
        return dataProcessor.process(origin, data);
    }

    /**
     * Processes expressions in a map, finding all expressions, evaluating them, and returning
     * a map with all original data plus processed data (processed data replaces unprocessed data).
     */
    @Override
    public Map<String, Object> evaluate(Map<String, Object> data) {
        return dataProcessor.process(data);
    }

    /**
     * Evaluates an expression with the given variables' context.
     */
    @Override
    public <T> T evaluate(String expression, Map<String, Object> variables, Class<T> resultType) {
        return executeWithExceptionHandling(
                expression,
                "evaluate expression",
                () -> !isExpression(expression)
                        ? literalValueConverter.convert(expression, resultType)
                        : evaluateSpelExpression(expression, variables, resultType)
        );
    }

    /**
     * Evaluates a boolean expression.
     */
    @Override
    public boolean evaluateBoolean(String expression, Map<String, Object> variables) {
        return executeWithExceptionHandling(expression, "evaluate boolean expression", () -> {
            var result = evaluate(expression, variables, Boolean.class);
            return Objects.requireNonNullElse(result, false);
        });
    }

    /**
     * Evaluates a string expression.
     */
    @Override
    public String evaluateString(String expression, Map<String, Object> variables) {
        return executeWithExceptionHandling(expression, "evaluate string expression", () ->
                evaluate(expression, variables, String.class));
    }

    /**
     * Checks if the given string is an expression (contains expression syntax).
     * Supports ${variable}, #{variable}, and {variable} syntax.
     */
    @Override
    public boolean isExpression(String value) {
        if (value == null || value.trim().isEmpty()) {
            return false;
        }
        return DOLLAR_PATTERN.matcher(value).find() ||
                HASH_PATTERN.matcher(value).find() ||
                BRACE_PATTERN.matcher(value).find();
    }

    /**
     * Resolves variables in a string by replacing expression placeholders.
     * For example, "Hello ${name}!" with name="World" becomes "Hello World!"
     * Only supports ${expression} syntax, not {expression} syntax.
     */
    public String resolveVariables(String text, Map<String, Object> variables) {
        // Only process dollar brace expressions ${...}, not simple brace expressions {...}
        if (!DOLLAR_PATTERN.matcher(text).find()) {
            return text;
        }

        var matcher = DOLLAR_CAPTURE_PATTERN.matcher(text);
        var result = new StringBuilder();
        while (matcher.find()) {
            var fullExpression = matcher.group();
            var expressionContent = matcher.group(1);

            String replacement = resolveExpressionReplacement(fullExpression, expressionContent, variables);
            matcher.appendReplacement(result, java.util.regex.Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(result);
        return result.toString();
    }

    private String resolveExpressionReplacement(String fullExpression, String expressionContent, Map<String, Object> variables) {
        try {
            var wrappedExpression = "${%s}".formatted(expressionContent);
            var value = evaluate(wrappedExpression, variables, Object.class);
            return determineReplacementValue(value, expressionContent, variables, fullExpression);
        } catch (Exception e) {
            log.warn("Failed to evaluate expression '{}'", fullExpression, e);
            return fullExpression;
        }
    }

    private String determineReplacementValue(Object value, String expressionContent, Map<String, Object> variables, String fullExpression) {
        if (value != null) {
            return value.toString();
        }

        // Check if this is a simple variable reference and if the variable exists but is null
        var variableName = getSimpleVariableName(expressionContent);
        // Variable exists but is null, return empty string
        return variableName == null || !variables.containsKey(variableName)
                ? fullExpression
                : EMPTY_STRING; // Variable doesn't exist, preserve expression
    }

    private String getSimpleVariableName(String expressionContent) {
        // For simple variable references like "name", return the variable name
        // For complex expressions like "user.name", return null
        return expressionContent != null && expressionContent.matches("^[a-zA-Z_][a-zA-Z0-9_]*$")
                ? expressionContent
                : null;
    }

    private <T> T executeWithExceptionHandling(String expression,
                                               String operation,
                                               Supplier<T> block) {
        try {
            return block.get();
        } catch (Exception e) {
            throw new ExpressionEvaluationException("Failed to %s '%s'".formatted(operation, expression), e);
        }
    }

    private <T> T evaluateSpelExpression(String expression,
                                         Map<String, Object> variables,
                                         Class<T> resultType) {
        var spelExpression = expressionConverter.convertToSpelExpression(expression);
        var context = createEvaluationContext(variables);
        return parser.parseExpression(spelExpression).getValue(context, resultType);
    }

    private StandardEvaluationContext createEvaluationContext(Map<String, Object> variables) {
        var context = new StandardEvaluationContext();
        setVariablesInContext(context, variables);
        setRootObjectIfNeeded(context, variables);
        return context;
    }

    private void setVariablesInContext(StandardEvaluationContext context, Map<String, Object> variables) {
        variables.forEach(context::setVariable);
    }

    private void setRootObjectIfNeeded(StandardEvaluationContext context, Map<String, Object> variables) {
        if (!variables.isEmpty()) {
            context.setRootObject(variables);
        }
    }

}