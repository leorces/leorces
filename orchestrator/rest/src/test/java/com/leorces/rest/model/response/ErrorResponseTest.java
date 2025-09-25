package com.leorces.rest.model.response;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ErrorResponse Tests")
class ErrorResponseTest {

    private static final String TEST_ERROR = "Test Error";
    private static final String TEST_MESSAGE = "Test error message";
    private static final int TEST_STATUS = 400;
    private static final LocalDateTime TEST_TIMESTAMP = LocalDateTime.of(2024, 1, 1, 12, 0, 0);
    private static final Map<String, String> TEST_VALIDATION_ERRORS = Map.of("field", "error");

    @Test
    @DisplayName("Should create ErrorResponse with all fields")
    void shouldCreateErrorResponseWithAllFields() {
        // Given & When
        var errorResponse = new ErrorResponse(
                TEST_ERROR,
                TEST_MESSAGE,
                TEST_STATUS,
                TEST_TIMESTAMP,
                TEST_VALIDATION_ERRORS
        );

        // Then
        assertThat(errorResponse.error()).isEqualTo(TEST_ERROR);
        assertThat(errorResponse.message()).isEqualTo(TEST_MESSAGE);
        assertThat(errorResponse.status()).isEqualTo(TEST_STATUS);
        assertThat(errorResponse.timestamp()).isEqualTo(TEST_TIMESTAMP);
        assertThat(errorResponse.validationErrors()).isEqualTo(TEST_VALIDATION_ERRORS);
    }

    @Test
    @DisplayName("Should create ErrorResponse with null values")
    void shouldCreateErrorResponseWithNullValues() {
        // Given & When
        var errorResponse = new ErrorResponse(null, null, 0, null, null);

        // Then
        assertThat(errorResponse.error()).isNull();
        assertThat(errorResponse.message()).isNull();
        assertThat(errorResponse.status()).isEqualTo(0);
        assertThat(errorResponse.timestamp()).isNull();
        assertThat(errorResponse.validationErrors()).isNull();
    }

    @Test
    @DisplayName("Should create ErrorResponse with empty validation errors")
    void shouldCreateErrorResponseWithEmptyValidationErrors() {
        // Given
        var emptyValidationErrors = Map.<String, String>of();

        // When
        var errorResponse = new ErrorResponse(
                TEST_ERROR,
                TEST_MESSAGE,
                TEST_STATUS,
                TEST_TIMESTAMP,
                emptyValidationErrors
        );

        // Then
        assertThat(errorResponse.validationErrors()).isEmpty();
    }

    @Test
    @DisplayName("Should create ErrorResponse with multiple validation errors")
    void shouldCreateErrorResponseWithMultipleValidationErrors() {
        // Given
        var multipleValidationErrors = Map.of(
                "field1", "error1",
                "field2", "error2",
                "field3", "error3"
        );

        // When
        var errorResponse = new ErrorResponse(
                TEST_ERROR,
                TEST_MESSAGE,
                TEST_STATUS,
                TEST_TIMESTAMP,
                multipleValidationErrors
        );

        // Then
        assertThat(errorResponse.validationErrors()).hasSize(3);
        assertThat(errorResponse.validationErrors().get("field1")).isEqualTo("error1");
        assertThat(errorResponse.validationErrors().get("field2")).isEqualTo("error2");
        assertThat(errorResponse.validationErrors().get("field3")).isEqualTo("error3");
    }

    @Test
    @DisplayName("Should create ErrorResponse with negative status code")
    void shouldCreateErrorResponseWithNegativeStatusCode() {
        // Given
        var negativeStatus = -1;

        // When
        var errorResponse = new ErrorResponse(
                TEST_ERROR,
                TEST_MESSAGE,
                negativeStatus,
                TEST_TIMESTAMP,
                TEST_VALIDATION_ERRORS
        );

        // Then
        assertThat(errorResponse.status()).isEqualTo(negativeStatus);
    }

    @Test
    @DisplayName("Should create ErrorResponse with zero status code")
    void shouldCreateErrorResponseWithZeroStatusCode() {
        // Given
        var zeroStatus = 0;

        // When
        var errorResponse = new ErrorResponse(
                TEST_ERROR,
                TEST_MESSAGE,
                zeroStatus,
                TEST_TIMESTAMP,
                TEST_VALIDATION_ERRORS
        );

        // Then
        assertThat(errorResponse.status()).isEqualTo(zeroStatus);
    }

    @Test
    @DisplayName("Should create ErrorResponse with large status code")
    void shouldCreateErrorResponseWithLargeStatusCode() {
        // Given
        var largeStatus = 999;

        // When
        var errorResponse = new ErrorResponse(
                TEST_ERROR,
                TEST_MESSAGE,
                largeStatus,
                TEST_TIMESTAMP,
                TEST_VALIDATION_ERRORS
        );

        // Then
        assertThat(errorResponse.status()).isEqualTo(largeStatus);
    }

    @Test
    @DisplayName("Should create ErrorResponse with empty strings")
    void shouldCreateErrorResponseWithEmptyStrings() {
        // Given
        var emptyError = "";
        var emptyMessage = "";

        // When
        var errorResponse = new ErrorResponse(
                emptyError,
                emptyMessage,
                TEST_STATUS,
                TEST_TIMESTAMP,
                TEST_VALIDATION_ERRORS
        );

        // Then
        assertThat(errorResponse.error()).isEmpty();
        assertThat(errorResponse.message()).isEmpty();
    }

    @Test
    @DisplayName("Should support equality comparison")
    void shouldSupportEqualityComparison() {
        // Given
        var errorResponse1 = new ErrorResponse(
                TEST_ERROR,
                TEST_MESSAGE,
                TEST_STATUS,
                TEST_TIMESTAMP,
                TEST_VALIDATION_ERRORS
        );
        var errorResponse2 = new ErrorResponse(
                TEST_ERROR,
                TEST_MESSAGE,
                TEST_STATUS,
                TEST_TIMESTAMP,
                TEST_VALIDATION_ERRORS
        );

        // When & Then
        assertThat(errorResponse1).isEqualTo(errorResponse2);
        assertThat(errorResponse1.hashCode()).isEqualTo(errorResponse2.hashCode());
    }

    @Test
    @DisplayName("Should support inequality comparison")
    void shouldSupportInequalityComparison() {
        // Given
        var errorResponse1 = new ErrorResponse(
                TEST_ERROR,
                TEST_MESSAGE,
                TEST_STATUS,
                TEST_TIMESTAMP,
                TEST_VALIDATION_ERRORS
        );
        var errorResponse2 = new ErrorResponse(
                "Different Error",
                TEST_MESSAGE,
                TEST_STATUS,
                TEST_TIMESTAMP,
                TEST_VALIDATION_ERRORS
        );

        // When & Then
        assertThat(errorResponse1).isNotEqualTo(errorResponse2);
        assertThat(errorResponse1.hashCode()).isNotEqualTo(errorResponse2.hashCode());
    }

    @Test
    @DisplayName("Should have meaningful toString representation")
    void shouldHaveMeaningfulToStringRepresentation() {
        // Given
        var errorResponse = new ErrorResponse(
                TEST_ERROR,
                TEST_MESSAGE,
                TEST_STATUS,
                TEST_TIMESTAMP,
                TEST_VALIDATION_ERRORS
        );

        // When
        var stringRepresentation = errorResponse.toString();

        // Then
        assertThat(stringRepresentation).contains("ErrorResponse");
        assertThat(stringRepresentation).contains(TEST_ERROR);
        assertThat(stringRepresentation).contains(TEST_MESSAGE);
        assertThat(stringRepresentation).contains(String.valueOf(TEST_STATUS));
    }

    @Test
    @DisplayName("Should create ErrorResponse with current timestamp")
    void shouldCreateErrorResponseWithCurrentTimestamp() {
        // Given
        var currentTime = LocalDateTime.now();

        // When
        var errorResponse = new ErrorResponse(
                TEST_ERROR,
                TEST_MESSAGE,
                TEST_STATUS,
                currentTime,
                TEST_VALIDATION_ERRORS
        );

        // Then
        assertThat(errorResponse.timestamp()).isEqualToIgnoringNanos(currentTime);
    }
}