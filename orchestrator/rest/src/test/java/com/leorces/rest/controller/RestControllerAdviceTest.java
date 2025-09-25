package com.leorces.rest.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayName("RestControllerAdvice Tests")
class RestControllerAdviceTest {

    private static final String TEST_FIELD_NAME = "testField";
    private static final String TEST_ERROR_MESSAGE = "Test error message";
    private static final String ILLEGAL_ARGUMENT_MESSAGE = "Invalid argument provided";
    private static final String GENERIC_EXCEPTION_MESSAGE = "Something went wrong";

    private RestControllerExceptionAdvice subject;

    @BeforeEach
    void setUp() {
        subject = new RestControllerExceptionAdvice();
    }

    @Test
    @DisplayName("Should handle MethodArgumentNotValidException with single field error")
    void shouldHandleMethodArgumentNotValidExceptionWithSingleFieldError() {
        // Given
        var mockException = mock(MethodArgumentNotValidException.class);
        var mockBindingResult = mock(BindingResult.class);
        var fieldError = new FieldError("testObject", TEST_FIELD_NAME, TEST_ERROR_MESSAGE);

        when(mockException.getBindingResult()).thenReturn(mockBindingResult);
        when(mockBindingResult.getAllErrors()).thenReturn(List.of(fieldError));

        // When
        var response = subject.handleValidationExceptions(mockException);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();

        var errorResponse = response.getBody();
        assertThat(errorResponse.error()).isEqualTo("Validation failed");
        assertThat(errorResponse.message()).isEqualTo("One or more fields have invalid values");
        assertThat(errorResponse.status()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(errorResponse.timestamp()).isCloseTo(LocalDateTime.now(), within(java.time.Duration.ofSeconds(1)));
        assertThat(errorResponse.validationErrors()).hasSize(1);
        assertThat(errorResponse.validationErrors().get(TEST_FIELD_NAME)).isEqualTo(TEST_ERROR_MESSAGE);
    }

    @Test
    @DisplayName("Should handle MethodArgumentNotValidException with multiple field errors")
    void shouldHandleMethodArgumentNotValidExceptionWithMultipleFieldErrors() {
        // Given
        var mockException = mock(MethodArgumentNotValidException.class);
        var mockBindingResult = mock(BindingResult.class);
        var fieldError1 = new FieldError("testObject", "field1", "Error message 1");
        var fieldError2 = new FieldError("testObject", "field2", "Error message 2");

        when(mockException.getBindingResult()).thenReturn(mockBindingResult);
        when(mockBindingResult.getAllErrors()).thenReturn(List.of(fieldError1, fieldError2));

        // When
        var response = subject.handleValidationExceptions(mockException);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();

        var errorResponse = response.getBody();
        assertThat(errorResponse.validationErrors()).hasSize(2);
        assertThat(errorResponse.validationErrors().get("field1")).isEqualTo("Error message 1");
        assertThat(errorResponse.validationErrors().get("field2")).isEqualTo("Error message 2");
    }

    @Test
    @DisplayName("Should handle IllegalArgumentException correctly")
    void shouldHandleIllegalArgumentException() {
        // Given
        var exception = new IllegalArgumentException(ILLEGAL_ARGUMENT_MESSAGE);

        // When
        var response = subject.handleIllegalArgumentException(exception);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();

        var errorResponse = response.getBody();
        assertThat(errorResponse.error()).isEqualTo("Invalid argument");
        assertThat(errorResponse.message()).isEqualTo(ILLEGAL_ARGUMENT_MESSAGE);
        assertThat(errorResponse.status()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(errorResponse.timestamp()).isCloseTo(LocalDateTime.now(), within(java.time.Duration.ofSeconds(1)));
        assertThat(errorResponse.validationErrors()).isNull();
    }

    @Test
    @DisplayName("Should handle generic Exception correctly")
    void shouldHandleGenericException() {
        // Given
        var exception = new RuntimeException(GENERIC_EXCEPTION_MESSAGE);

        // When
        var response = subject.handleGenericException(exception);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();

        var errorResponse = response.getBody();
        assertThat(errorResponse.error()).isEqualTo("Internal server error");
        assertThat(errorResponse.message()).isEqualTo("An unexpected error occurred. Please try again later.");
        assertThat(errorResponse.status()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR.value());
        assertThat(errorResponse.timestamp()).isCloseTo(LocalDateTime.now(), within(java.time.Duration.ofSeconds(1)));
        assertThat(errorResponse.validationErrors()).isNull();
    }

    @Test
    @DisplayName("Should handle MethodArgumentNotValidException with empty validation errors")
    void shouldHandleMethodArgumentNotValidExceptionWithEmptyErrors() {
        // Given
        var mockException = mock(MethodArgumentNotValidException.class);
        var mockBindingResult = mock(BindingResult.class);

        when(mockException.getBindingResult()).thenReturn(mockBindingResult);
        when(mockBindingResult.getAllErrors()).thenReturn(List.of());

        // When
        var response = subject.handleValidationExceptions(mockException);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();

        var errorResponse = response.getBody();
        assertThat(errorResponse.validationErrors()).isEmpty();
    }

    @Test
    @DisplayName("Should handle IllegalArgumentException with null message")
    void shouldHandleIllegalArgumentExceptionWithNullMessage() {
        // Given
        var exception = new IllegalArgumentException((String) null);

        // When
        var response = subject.handleIllegalArgumentException(exception);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();

        var errorResponse = response.getBody();
        assertThat(errorResponse.error()).isEqualTo("Invalid argument");
        assertThat(errorResponse.message()).isNull();
        assertThat(errorResponse.status()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(errorResponse.validationErrors()).isNull();
    }
}