package com.leorces.juel.converter;


import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Pattern;


/**
 * Handles the conversion of JUEL-style expressions to SpEL-style expressions.
 */
@Component
public class SpelExpressionConverter {

    private static final Pattern DOLLAR_PATTERN = Pattern.compile("\\$\\{([^}]+)}");
    private static final Pattern BRACE_PATTERN = Pattern.compile("\\{([^}]+)}");
    private static final Pattern DOUBLE_QUOTE_PATTERN = Pattern.compile("\"[^\"]*\"");
    private static final Pattern SINGLE_QUOTE_PATTERN = Pattern.compile("'[^']*'");
    private static final Pattern TOKEN_PATTERN = Pattern.compile("\\b[a-zA-Z_][a-zA-Z0-9_]*(?:\\.[a-zA-Z_][a-zA-Z0-9_]*)*\\b");
    private static final Pattern CHAINED_METHOD_PATTERN = Pattern.compile("\\)\\.([a-zA-Z_][a-zA-Z0-9_]*)\\s*\\(");
    private static final Pattern NUMERIC_PATTERN = Pattern.compile("\\d+(\\.\\d+)?");
    private static final String QUOTED_STRING_PLACEHOLDER_FORMAT = "__QUOTED_STRING_%d__";
    private static final String CHAINED_METHOD_PLACEHOLDER_FORMAT = "__CHAINED_METHOD_%d__(";
    private static final String QUOTED_STRING_PREFIX = "__QUOTED_STRING_";
    private static final String CHAINED_METHOD_PREFIX = "__CHAINED_METHOD_";
    private static final Set<String> RESERVED_KEYWORDS = Set.of(
            "true", "false", "null", "and", "or", "not",
            "eq", "ne", "lt", "le", "gt", "ge"
    );


    /**
     * Converts JUEL-style expressions (${variable}) to SpEL-style expressions.
     * Also handles property access like ${client.firstName}.
     *
     * @param expression the expression to convert
     * @return converted SpEL expression
     */
    public String convertToSpelExpression(String expression) {
        if (expression == null) {
            return null;
        }
        var spelExpression = convertPatternMatches(expression, DOLLAR_PATTERN);
        return convertPatternMatches(spelExpression, BRACE_PATTERN).trim();
    }

    private String convertPatternMatches(String expression, Pattern pattern) {
        return processPatternMatches(expression, pattern, match -> convertVariableReferences(match.group(1)));
    }

    private String convertVariableReferences(String content) {
        var quotedStrings = new ArrayList<String>();
        var tempContent = preserveQuotedStrings(content, quotedStrings);
        tempContent = replaceVariableReferences(tempContent);
        return restoreQuotedStrings(tempContent, quotedStrings);
    }

    private String preserveQuotedStrings(String content, List<String> quotedStrings) {
        var result = preserveStringPattern(content, quotedStrings, DOUBLE_QUOTE_PATTERN);
        return preserveStringPattern(result, quotedStrings, SINGLE_QUOTE_PATTERN);
    }

    private String preserveStringPattern(String content, List<String> quotedStrings, Pattern pattern) {
        return processPatternMatches(content, pattern, match -> {
            var placeholder = createPlaceholder(quotedStrings.size());
            quotedStrings.add(match.group());
            return placeholder;
        });
    }

    private String createPlaceholder(int index) {
        return String.format(QUOTED_STRING_PLACEHOLDER_FORMAT, index);
    }

    private String replaceVariableReferences(String content) {
        var chainedMethods = new ArrayList<String>();
        var result = preserveChainedMethods(content, chainedMethods);
        result = processVariableTokens(result);
        return restoreChainedMethods(result, chainedMethods);
    }

    private String processVariableTokens(String content) {
        return processPatternMatches(content, TOKEN_PATTERN, match -> {
            var token = match.group();
            return convertToken(token, match.end(), content);
        });
    }

    private String convertToken(String token, int endIndex, String content) {
        if (shouldSkipToken(token)) {
            return token;
        }

        return token.contains(".")
                ? convertPropertyAccess(token, endIndex, content)
                : "#%s".formatted(token);
    }

    private boolean shouldSkipToken(String token) {
        return token.startsWith(QUOTED_STRING_PREFIX) ||
                token.startsWith(CHAINED_METHOD_PREFIX) ||
                NUMERIC_PATTERN.matcher(token).matches() ||
                RESERVED_KEYWORDS.contains(token);
    }

    private String convertPropertyAccess(String token, int endIndex, String content) {
        char nextChar = endIndex < content.length()
                ? content.charAt(endIndex)
                : ' ';

        return nextChar == '('
                ? convertMethodCall(token)
                : convertPropertyPath(token);
    }

    private String convertMethodCall(String token) {
        var parts = token.split("\\.");
        if (parts.length == 1) {
            return token;
        }
        var rootVar = parts[0];
        var result = new StringBuilder("#%s".formatted(rootVar));
        for (int i = 1; i < parts.length - 1; i++) {
            result.append("['").append(parts[i]).append("']");
        }
        result.append(".").append(parts[parts.length - 1]);
        return result.toString();
    }

    private String convertPropertyPath(String token) {
        var parts = token.split("\\.");
        var rootVar = parts[0];
        var result = new StringBuilder("#%s".formatted(rootVar));
        for (int i = 1; i < parts.length; i++) {
            result.append("['").append(parts[i]).append("']");
        }
        return result.toString();
    }

    private String preserveChainedMethods(String content, List<String> chainedMethods) {
        return processPatternMatches(content, CHAINED_METHOD_PATTERN, match -> {
            var placeholder = createChainedMethodPlaceholder(chainedMethods.size());
            var chainedMethod = ").%s(".formatted(match.group(1));
            chainedMethods.add(chainedMethod);
            return placeholder;
        });
    }

    private String createChainedMethodPlaceholder(int index) {
        return String.format(CHAINED_METHOD_PLACEHOLDER_FORMAT, index);
    }

    private String restoreChainedMethods(String content, List<String> chainedMethods) {
        return restorePlaceholders(content, chainedMethods, this::createChainedMethodPlaceholder);
    }

    private String restoreQuotedStrings(String content, List<String> quotedStrings) {
        return restorePlaceholders(content, quotedStrings, this::createPlaceholder);
    }

    private String processPatternMatches(String input, Pattern pattern, Function<java.util.regex.Matcher, String> replacementFunction) {
        var matcher = pattern.matcher(input);
        var result = new StringBuilder();
        while (matcher.find()) {
            var replacement = replacementFunction.apply(matcher);
            matcher.appendReplacement(result, replacement);
        }
        matcher.appendTail(result);
        return result.toString();
    }

    private String restorePlaceholders(String content, List<String> values, Function<Integer, String> placeholderFunction) {
        var result = content;
        for (int i = 0; i < values.size(); i++) {
            var placeholder = placeholderFunction.apply(i);
            result = result.replace(placeholder, values.get(i));
        }
        return result;
    }
}