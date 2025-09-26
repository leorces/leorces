package com.leorces.juel.resolver;


import com.leorces.juel.ExpressionEvaluator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.function.UnaryOperator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Handles variable resolution in text by replacing expression placeholders.
 * Supports both ${expression} and {expression} syntax.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class VariableResolver {

    private static final Pattern EXPRESSION_PATTERN = Pattern.compile("\\$\\{([^}]+)}");
    private static final Pattern BRACE_PATTERN = Pattern.compile("\\{([^}]+)}");
    private static final UnaryOperator<String> IDENTITY_TRANSFORMER = content -> content;
    private static final UnaryOperator<String> BRACE_TO_EXPRESSION_TRANSFORMER = "${%s}"::formatted;

    private final ExpressionEvaluator expressionEvaluator;

    /**
     * Resolves variables in a string by replacing expression placeholders.
     * For example, "Hello ${name}!" with name="World" becomes "Hello World!"
     * Supports both ${expression} and {expression} syntax.
     */
    public String resolveVariables(String text, Map<String, Object> variables) {
        if (text == null) {
            return null;
        }
        // First process dollar brace expressions ${...}
        var result = EXPRESSION_PATTERN.matcher(text).find()
                ? resolveExpressionPattern(text, variables, EXPRESSION_PATTERN, IDENTITY_TRANSFORMER)
                : text;

        // Then process simple brace expressions {...}
        return BRACE_PATTERN.matcher(result).find()
                ? resolveExpressionPattern(result, variables, BRACE_PATTERN, BRACE_TO_EXPRESSION_TRANSFORMER)
                : result;
    }

    private String resolveExpressionPattern(String text, Map<String, Object> variables,
                                            Pattern pattern,
                                            UnaryOperator<String> transformer) {
        var matcher = pattern.matcher(text);
        var result = new StringBuilder();
        while (matcher.find()) {
            String replacement = processExpressionMatch(matcher, variables, transformer);
            matcher.appendReplacement(result, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(result);
        return result.toString();
    }

    private String processExpressionMatch(Matcher matcher,
                                          Map<String, Object> variables,
                                          UnaryOperator<String> transformer) {
        var fullExpression = matcher.group();
        var expressionContent = matcher.group(1);
        var transformedExpression = transformer.apply(expressionContent);
        try {
            // For IDENTITY_TRANSFORMER, transformedExpression is just the content without ${}
            // For BRACE_TO_EXPRESSION_TRANSFORMER, transformedExpression already has ${}
            var wrappedExpression = transformedExpression.startsWith("${")
                    ? transformedExpression
                    : "${%s}".formatted(transformedExpression);

            var value = expressionEvaluator.evaluate(wrappedExpression, variables, Object.class);

            if (value != null) {
                return value.toString();
            }

            // Check if this is a simple variable reference and if the variable exists but is null
            var variableName = getSimpleVariableName(expressionContent);
            // Variable exists but is null, return empty string
            return variableName == null || !variables.containsKey(variableName)
                    ? fullExpression
                    : ""; // Variable doesn't exist, preserve expression
        } catch (Exception e) {
            log.warn("Failed to evaluate expression '{}'", transformedExpression, e);
            return fullExpression;
        }
    }

    private String getSimpleVariableName(String expressionContent) {
        // For simple variable references like "name", return the variable name
        // For complex expressions like "user.name", return null
        return expressionContent != null && expressionContent.matches("^[a-zA-Z_][a-zA-Z0-9_]*$")
                ? expressionContent
                : null;
    }

}