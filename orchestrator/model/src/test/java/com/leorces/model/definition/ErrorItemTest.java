package com.leorces.model.definition;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Error Item Tests")
class ErrorItemTest {

    private static final String TEST_NAME = "ValidationError";
    private static final String TEST_ERROR_CODE = "VALIDATION_001";
    private static final String TEST_MESSAGE = "Invalid input provided";

    @Test
    @DisplayName("Should create ErrorItem with all fields using builder")
    void shouldCreateErrorItemWithAllFields() {
        // When
        var errorItem = ErrorItem.builder()
                .name(TEST_NAME)
                .errorCode(TEST_ERROR_CODE)
                .message(TEST_MESSAGE)
                .build();

        // Then
        assertNotNull(errorItem);
        assertEquals(TEST_NAME, errorItem.name());
        assertEquals(TEST_ERROR_CODE, errorItem.errorCode());
        assertEquals(TEST_MESSAGE, errorItem.message());
    }

    @Test
    @DisplayName("Should create ErrorItem with null fields")
    void shouldCreateErrorItemWithNullFields() {
        // When
        var errorItem = ErrorItem.builder()
                .name(null)
                .errorCode(null)
                .message(null)
                .build();

        // Then
        assertNotNull(errorItem);
        assertNull(errorItem.name());
        assertNull(errorItem.errorCode());
        assertNull(errorItem.message());
    }

    @Test
    @DisplayName("Should create ErrorItem with minimal fields")
    void shouldCreateErrorItemWithMinimalFields() {
        // When
        var errorItem = ErrorItem.builder()
                .name(TEST_NAME)
                .build();

        // Then
        assertNotNull(errorItem);
        assertEquals(TEST_NAME, errorItem.name());
        assertNull(errorItem.errorCode());
        assertNull(errorItem.message());
    }

    @Test
    @DisplayName("Should implement equals correctly")
    void shouldImplementEqualsCorrectly() {
        // Given
        var errorItem1 = ErrorItem.builder()
                .name(TEST_NAME)
                .errorCode(TEST_ERROR_CODE)
                .message(TEST_MESSAGE)
                .build();

        var errorItem2 = ErrorItem.builder()
                .name(TEST_NAME)
                .errorCode(TEST_ERROR_CODE)
                .message(TEST_MESSAGE)
                .build();

        var errorItem3 = ErrorItem.builder()
                .name("DifferentName")
                .errorCode(TEST_ERROR_CODE)
                .message(TEST_MESSAGE)
                .build();

        // When & Then
        assertEquals(errorItem1, errorItem2);
        assertNotEquals(errorItem1, errorItem3);
        assertNotEquals(null, errorItem1);
    }

    @Test
    @DisplayName("Should implement hashCode correctly")
    void shouldImplementHashCodeCorrectly() {
        // Given
        var errorItem1 = ErrorItem.builder()
                .name(TEST_NAME)
                .errorCode(TEST_ERROR_CODE)
                .message(TEST_MESSAGE)
                .build();

        var errorItem2 = ErrorItem.builder()
                .name(TEST_NAME)
                .errorCode(TEST_ERROR_CODE)
                .message(TEST_MESSAGE)
                .build();

        // When & Then
        assertEquals(errorItem1.hashCode(), errorItem2.hashCode());
    }

    @Test
    @DisplayName("Should implement toString correctly")
    void shouldImplementToStringCorrectly() {
        // Given
        var errorItem = ErrorItem.builder()
                .name(TEST_NAME)
                .errorCode(TEST_ERROR_CODE)
                .message(TEST_MESSAGE)
                .build();

        // When
        var toStringResult = errorItem.toString();

        // Then
        assertNotNull(toStringResult);
        assertTrue(toStringResult.contains("ErrorItem"));
        assertTrue(toStringResult.contains(TEST_NAME));
        assertTrue(toStringResult.contains(TEST_ERROR_CODE));
        assertTrue(toStringResult.contains(TEST_MESSAGE));
    }

    @Test
    @DisplayName("Should handle empty strings correctly")
    void shouldHandleEmptyStringsCorrectly() {
        // Given
        var emptyName = "";
        var emptyErrorCode = "";
        var emptyMessage = "";

        // When
        var errorItem = ErrorItem.builder()
                .name(emptyName)
                .errorCode(emptyErrorCode)
                .message(emptyMessage)
                .build();

        // Then
        assertNotNull(errorItem);
        assertEquals(emptyName, errorItem.name());
        assertEquals(emptyErrorCode, errorItem.errorCode());
        assertEquals(emptyMessage, errorItem.message());
    }

}