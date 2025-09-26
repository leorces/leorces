package com.leorces.juel.converter;


import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


@DisplayName("LiteralValueConverter Tests")
class LiteralValueConverterTest {

    private static final String VALID_INTEGER_STRING = "42";
    private static final String VALID_BOOLEAN_TRUE_STRING = "true";
    private static final String VALID_BOOLEAN_FALSE_STRING = "false";
    private static final String VALID_DOUBLE_STRING = "3.14";
    private static final String INVALID_NUMBER_STRING = "not_a_number";
    private static final String WHITESPACE_STRING = "  123  ";
    private static final String TEST_STRING_VALUE = "test_value";
    private final LiteralValueConverter converter = new LiteralValueConverter();

    @Test
    @DisplayName("Should convert string to String type")
    void shouldConvertStringToStringType() {
        //When
        var result = converter.convert(TEST_STRING_VALUE, String.class);

        //Then
        assertThat(result).isEqualTo(TEST_STRING_VALUE);
        assertThat(result).isInstanceOf(String.class);
    }

    @Test
    @DisplayName("Should convert valid string to Integer")
    void shouldConvertValidStringToInteger() {
        //When
        var result = converter.convert(VALID_INTEGER_STRING, Integer.class);

        //Then
        assertThat(result).isEqualTo(42);
        assertThat(result).isInstanceOf(Integer.class);
    }

    @Test
    @DisplayName("Should convert string with whitespace to Integer")
    void shouldConvertStringWithWhitespaceToInteger() {
        //When
        var result = converter.convert(WHITESPACE_STRING, Integer.class);

        //Then
        assertThat(result).isEqualTo(123);
        assertThat(result).isInstanceOf(Integer.class);
    }

    @Test
    @DisplayName("Should return null for invalid Integer string")
    void shouldReturnNullForInvalidIntegerString() {
        //When
        var result = converter.convert(INVALID_NUMBER_STRING, Integer.class);

        //Then
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("Should return null for null Integer string")
    void shouldReturnNullForNullIntegerString() {
        //When
        var result = converter.convert(null, Integer.class);

        //Then
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("Should convert valid true string to Boolean")
    void shouldConvertValidTrueStringToBoolean() {
        //When
        var result = converter.convert(VALID_BOOLEAN_TRUE_STRING, Boolean.class);

        //Then
        assertThat(result).isEqualTo(true);
        assertThat(result).isInstanceOf(Boolean.class);
    }

    @Test
    @DisplayName("Should convert valid false string to Boolean")
    void shouldConvertValidFalseStringToBoolean() {
        //When
        var result = converter.convert(VALID_BOOLEAN_FALSE_STRING, Boolean.class);

        //Then
        assertThat(result).isEqualTo(false);
        assertThat(result).isInstanceOf(Boolean.class);
    }

    @Test
    @DisplayName("Should convert case insensitive boolean strings")
    void shouldConvertCaseInsensitiveBooleanStrings() {
        //Given & When & Then
        assertThat(converter.convert("TRUE", Boolean.class)).isEqualTo(true);
        assertThat(converter.convert("False", Boolean.class)).isEqualTo(false);
        assertThat(converter.convert("tRuE", Boolean.class)).isEqualTo(true);
        assertThat(converter.convert("  true  ", Boolean.class)).isEqualTo(true);
    }

    @Test
    @DisplayName("Should return null for invalid Boolean string")
    void shouldReturnNullForInvalidBooleanString() {
        //When
        var result = converter.convert(INVALID_NUMBER_STRING, Boolean.class);

        //Then
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("Should return null for null Boolean string")
    void shouldReturnNullForNullBooleanString() {
        //When
        var result = converter.convert(null, Boolean.class);

        //Then
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("Should convert valid string to Double")
    void shouldConvertValidStringToDouble() {
        //When
        var result = converter.convert(VALID_DOUBLE_STRING, Double.class);

        //Then
        assertThat(result).isEqualTo(3.14);
        assertThat(result).isInstanceOf(Double.class);
    }

    @Test
    @DisplayName("Should convert string with whitespace to Double")
    void shouldConvertStringWithWhitespaceToDouble() {
        //When
        var result = converter.convert("  2.5  ", Double.class);

        //Then
        assertThat(result).isEqualTo(2.5);
        assertThat(result).isInstanceOf(Double.class);
    }

    @Test
    @DisplayName("Should return null for invalid Double string")
    void shouldReturnNullForInvalidDoubleString() {
        //When
        var result = converter.convert(INVALID_NUMBER_STRING, Double.class);

        //Then
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("Should throw RuntimeException for null Double string")
    void shouldThrowRuntimeExceptionForNullDoubleString() {
        //When & Then
        assertThatThrownBy(() -> converter.convert(null, Double.class))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Cannot convert null to Double");
    }

    @Test
    @DisplayName("Should cast to generic type for unsupported types")
    void shouldCastToGenericTypeForUnsupportedTypes() {
        //When
        var result = converter.convert(TEST_STRING_VALUE, Object.class);

        //Then
        assertThat(result).isEqualTo(TEST_STRING_VALUE);
        assertThat(result).isInstanceOf(Object.class);
        assertThat(result).isInstanceOf(String.class);
    }

    @Test
    @DisplayName("Should handle custom class type")
    void shouldHandleCustomClassType() {
        //When
        var result = converter.convert(TEST_STRING_VALUE, CharSequence.class);

        //Then
        assertThat(result).isEqualTo(TEST_STRING_VALUE);
        assertThat(result).isInstanceOf(CharSequence.class);
    }

}