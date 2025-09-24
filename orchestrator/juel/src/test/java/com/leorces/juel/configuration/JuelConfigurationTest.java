package com.leorces.juel.configuration;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;

import static org.junit.jupiter.api.Assertions.*;


class JuelConfigurationTest {

    private JuelConfiguration juelConfiguration;

    @BeforeEach
    void setUp() {
        juelConfiguration = new JuelConfiguration();
    }

    @Test
    @DisplayName("Should create SpelExpressionParser bean")
    void shouldCreateSpelExpressionParserBean() {
        // When
        var result = juelConfiguration.expressionParser();

        // Then
        assertNotNull(result);
        assertInstanceOf(ExpressionParser.class, result);
        assertInstanceOf(SpelExpressionParser.class, result);
    }

    @Test
    @DisplayName("Should create new instances on multiple calls")
    void shouldCreateNewInstancesOnMultipleCalls() {
        // When
        var parser1 = juelConfiguration.expressionParser();
        var parser2 = juelConfiguration.expressionParser();

        // Then
        assertNotNull(parser1);
        assertNotNull(parser2);
        assertNotSame(parser1, parser2);
    }

    @Test
    @DisplayName("Should create functional expression parser")
    void shouldCreateFunctionalExpressionParser() {
        // Given
        var parser = juelConfiguration.expressionParser();
        var expression = "'Hello ' + 'World'";

        // When
        var parsedExpression = parser.parseExpression(expression);
        var result = parsedExpression.getValue(String.class);

        // Then
        assertEquals("Hello World", result);
    }
}