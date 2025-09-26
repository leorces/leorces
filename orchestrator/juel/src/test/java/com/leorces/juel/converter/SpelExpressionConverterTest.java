package com.leorces.juel.converter;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;


@DisplayName("SpelExpressionConverter Tests")
class SpelExpressionConverterTest {

    private SpelExpressionConverter converter;

    @BeforeEach
    void setUp() {
        converter = new SpelExpressionConverter();
    }

    @Test
    @DisplayName("Should convert simple dollar brace variable")
    void shouldConvertSimpleDollarBraceVariable() {
        //When
        var result = converter.convertToSpelExpression("${username}");

        //Then
        assertThat(result).isEqualTo("#username");
    }

    @Test
    @DisplayName("Should convert simple brace variable")
    void shouldConvertSimpleBraceVariable() {
        //When
        var result = converter.convertToSpelExpression("{username}");

        //Then
        assertThat(result).isEqualTo("#username");
    }

    @Test
    @DisplayName("Should convert property access")
    void shouldConvertPropertyAccess() {
        //When
        var result = converter.convertToSpelExpression("${user.name}");

        //Then
        assertThat(result).isEqualTo("#user['name']");
    }

    @Test
    @DisplayName("Should convert nested property access")
    void shouldConvertNestedPropertyAccess() {
        //When
        var result = converter.convertToSpelExpression("${user.profile.name}");

        //Then
        assertThat(result).isEqualTo("#user['profile']['name']");
    }

    @Test
    @DisplayName("Should convert method call")
    void shouldConvertMethodCall() {
        //When
        var result = converter.convertToSpelExpression("${user.getName()}");

        //Then
        assertThat(result).isEqualTo("#user.getName()");
    }

    @Test
    @DisplayName("Should convert complex method call with nested property")
    void shouldConvertComplexMethodCallWithNestedProperty() {
        //When
        var result = converter.convertToSpelExpression("${user.profile.getName()}");

        //Then
        assertThat(result).isEqualTo("#user['profile'].getName()");
    }

    @Test
    @DisplayName("Should preserve quoted strings in expressions")
    void shouldPreserveQuotedStringsInExpressions() {
        //When
        var result = converter.convertToSpelExpression("${user.getName() == \"John Doe\"}");

        //Then
        assertThat(result).isEqualTo("#user.getName() == \"John Doe\"");
    }

    @Test
    @DisplayName("Should preserve single quoted strings")
    void shouldPreserveSingleQuotedStrings() {
        //When
        var result = converter.convertToSpelExpression("${user.getName() == 'John Doe'}");

        //Then
        assertThat(result).isEqualTo("#user.getName() == 'John Doe'");
    }

    @Test
    @DisplayName("Should handle mixed quote types")
    void shouldHandleMixedQuoteTypes() {
        //When
        var result = converter.convertToSpelExpression("${user.getName() == \"John\" and user.getLastName() == 'Doe'}");

        //Then
        assertThat(result).isEqualTo("#user.getName() == \"John\" and #user.getLastName() == 'Doe'");
    }

    @Test
    @DisplayName("Should preserve numeric literals")
    void shouldPreserveNumericLiterals() {
        //When
        var result = converter.convertToSpelExpression("${user.age > 25}");

        //Then
        assertThat(result).isEqualTo("#user['age'] > 25");
    }

    @Test
    @DisplayName("Should preserve decimal literals")
    void shouldPreserveDecimalLiterals() {
        //When
        var result = converter.convertToSpelExpression("${user.balance > 100.50}");

        //Then
        assertThat(result).isEqualTo("#user['balance'] > 100.50");
    }

    @Test
    @DisplayName("Should preserve reserved keywords")
    void shouldPreserveReservedKeywords() {
        //When
        var result = converter.convertToSpelExpression("${user.active == true and user.name != null}");

        //Then
        assertThat(result).isEqualTo("#user['active'] == true and #user['name'] != null");
    }

    @Test
    @DisplayName("Should handle logical operators with reserved words")
    void shouldHandleLogicalOperatorsWithReservedWords() {
        //When
        var result = converter.convertToSpelExpression("${user.active eq true or user.deleted ne true}");

        //Then
        assertThat(result).isEqualTo("#user['active'] eq true or #user['deleted'] ne true");
    }

    @Test
    @DisplayName("Should handle comparison operators")
    void shouldHandleComparisonOperators() {
        //When
        var result = converter.convertToSpelExpression("${user.age gt 18 and user.age lt 65}");

        //Then
        assertThat(result).isEqualTo("#user['age'] gt 18 and #user['age'] lt 65");
    }

    @Test
    @DisplayName("Should convert multiple variables in same expression")
    void shouldConvertMultipleVariablesInSameExpression() {
        //When
        var result = converter.convertToSpelExpression("${firstName} + \" \" + ${lastName}");

        //Then
        assertThat(result).isEqualTo("#firstName + \" \" + #lastName");
    }

    @Test
    @DisplayName("Should handle mixed brace types in same expression")
    void shouldHandleMixedBraceTypesInSameExpression() {
        //When
        var result = converter.convertToSpelExpression("${firstName} + {lastName}");

        //Then
        assertThat(result).isEqualTo("#firstName + #lastName");
    }

    @Test
    @DisplayName("Should handle chained method calls")
    void shouldHandleChainedMethodCalls() {
        //When
        var result = converter.convertToSpelExpression("${user.getProfile().getName()}");

        //Then
        assertThat(result).isEqualTo("#user.getProfile().getName()");
    }

    @Test
    @DisplayName("Should handle complex chained operations")
    void shouldHandleComplexChainedOperations() {
        //When
        var result = converter.convertToSpelExpression("${user.getProfile().getName().toLowerCase()}");

        //Then
        assertThat(result).isEqualTo("#user.getProfile().getName().toLowerCase()");
    }

    @Test
    @DisplayName("Should return original expression when no conversion needed")
    void shouldReturnOriginalExpressionWhenNoConversionNeeded() {
        //When
        var result = converter.convertToSpelExpression("simple text without expressions");

        //Then
        assertThat(result).isEqualTo("simple text without expressions");
    }

    @Test
    @DisplayName("Should handle empty expression")
    void shouldHandleEmptyExpression() {
        //Given
        var expression = "";

        //When
        var result = converter.convertToSpelExpression(expression);

        //Then
        assertThat(result).isEqualTo("");
    }

    @Test
    @DisplayName("Should handle null expression")
    void shouldHandleNullExpression() {
        //When
        var result = converter.convertToSpelExpression(null);

        //Then
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("Should handle expression with only braces")
    void shouldHandleExpressionWithOnlyBraces() {
        //When
        var result = converter.convertToSpelExpression("${}");

        //Then
        assertThat(result).isEqualTo("${}");
    }

    @Test
    @DisplayName("Should handle expressions with whitespace")
    void shouldHandleExpressionsWithWhitespace() {
        //When
        var result = converter.convertToSpelExpression("${ user.name }");

        //Then
        assertThat(result).isEqualTo("#user['name']");
    }

    @Test
    @DisplayName("Should handle complex boolean expressions")
    void shouldHandleComplexBooleanExpressions() {
        //When
        var result = converter.convertToSpelExpression("${(user.age >= 18 and user.active == true) or user.admin == true}");

        //Then
        assertThat(result).isEqualTo("(#user['age'] >= 18 and #user['active'] == true) or #user['admin'] == true");
    }

    @Test
    @DisplayName("Should handle method calls with parameters")
    void shouldHandleMethodCallsWithParameters() {
        //When
        var result = converter.convertToSpelExpression("${user.hasPermission('READ')}");

        //Then
        assertThat(result).isEqualTo("#user.hasPermission('READ')");
    }

    @Test
    @DisplayName("Should handle method calls with multiple parameters")
    void shouldHandleMethodCallsWithMultipleParameters() {
        //When
        var result = converter.convertToSpelExpression("${user.hasPermission('READ', 'WRITE')}");

        //Then
        assertThat(result).isEqualTo("#user.hasPermission('READ', 'WRITE')");
    }

    @Test
    @DisplayName("Should handle arithmetic expressions")
    void shouldHandleArithmeticExpressions() {
        //When
        var result = converter.convertToSpelExpression("${user.age + 10 * 2}");

        //Then
        assertThat(result).isEqualTo("#user['age'] + 10 * 2");
    }

    @Test
    @DisplayName("Should handle variable names with underscores and numbers")
    void shouldHandleVariableNamesWithUnderscoresAndNumbers() {
        //When
        var result = converter.convertToSpelExpression("${user_name_1 + user2.profile_data}");

        //Then
        assertThat(result).isEqualTo("#user_name_1 + #user2['profile_data']");
    }

    @Test
    @DisplayName("Should handle expressions with parentheses")
    void shouldHandleExpressionsWithParentheses() {
        //When
        var result = converter.convertToSpelExpression("${(user.age + 5) > (limit * 2)}");

        //Then
        assertThat(result).isEqualTo("(#user['age'] + 5) > (#limit * 2)");
    }

}