package com.leorces.model.definition.activity.event.end;

import com.leorces.model.definition.activity.ActivityType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Error End Event Tests")
class ErrorEndEventTest {

    private static final String TEST_ID = "errorEndEvent1";
    private static final String TEST_PARENT_ID = "subprocess1";
    private static final String TEST_NAME = "Error End Event";
    private static final List<String> TEST_INCOMING = List.of("task1", "gateway2");
    private static final List<String> TEST_OUTGOING = List.of();
    private static final String TEST_ERROR_CODE = "BUSINESS_ERROR";

    @Test
    @DisplayName("Should create ErrorEndEvent with all fields using builder")
    void shouldCreateErrorEndEventWithAllFields() {
        // When
        var errorEndEvent = ErrorEndEvent.builder()
                .id(TEST_ID)
                .parentId(TEST_PARENT_ID)
                .name(TEST_NAME)
                .type(ActivityType.ERROR_END_EVENT)
                .incoming(TEST_INCOMING)
                .outgoing(TEST_OUTGOING)
                .errorCode(TEST_ERROR_CODE)
                .build();

        // Then
        assertNotNull(errorEndEvent);
        assertEquals(TEST_ID, errorEndEvent.id());
        assertEquals(TEST_PARENT_ID, errorEndEvent.parentId());
        assertEquals(TEST_NAME, errorEndEvent.name());
        assertEquals(ActivityType.ERROR_END_EVENT, errorEndEvent.type());
        assertEquals(TEST_INCOMING, errorEndEvent.incoming());
        assertEquals(TEST_OUTGOING, errorEndEvent.outgoing());
        assertEquals(TEST_ERROR_CODE, errorEndEvent.errorCode());
    }

    @Test
    @DisplayName("Should create ErrorEndEvent with null fields")
    void shouldCreateErrorEndEventWithNullFields() {
        // When
        var errorEndEvent = ErrorEndEvent.builder()
                .id(TEST_ID)
                .parentId(null)
                .name(null)
                .type(null)
                .incoming(null)
                .outgoing(null)
                .errorCode(null)
                .build();

        // Then
        assertNotNull(errorEndEvent);
        assertEquals(TEST_ID, errorEndEvent.id());
        assertNull(errorEndEvent.parentId());
        assertNull(errorEndEvent.name());
        assertEquals(ActivityType.ERROR_END_EVENT, errorEndEvent.type());
        assertNull(errorEndEvent.incoming());
        assertNull(errorEndEvent.outgoing());
        assertNull(errorEndEvent.errorCode());
    }

    @Test
    @DisplayName("Should create ErrorEndEvent with minimal fields")
    void shouldCreateErrorEndEventWithMinimalFields() {
        // When
        var errorEndEvent = ErrorEndEvent.builder()
                .id(TEST_ID)
                .name(TEST_NAME)
                .errorCode(TEST_ERROR_CODE)
                .build();

        // Then
        assertNotNull(errorEndEvent);
        assertEquals(TEST_ID, errorEndEvent.id());
        assertEquals(TEST_NAME, errorEndEvent.name());
        assertEquals(TEST_ERROR_CODE, errorEndEvent.errorCode());
        assertEquals(ActivityType.ERROR_END_EVENT, errorEndEvent.type());
        assertNull(errorEndEvent.parentId());
        assertNull(errorEndEvent.incoming());
        assertNull(errorEndEvent.outgoing());
    }

    @Test
    @DisplayName("Should override type to return ERROR_END_EVENT")
    void shouldOverrideTypeToReturnErrorEndEvent() {
        // Given
        var errorEndEvent = ErrorEndEvent.builder()
                .id(TEST_ID)
                .type(ActivityType.START_EVENT) // Different type in constructor
                .build();

        // When & Then
        assertEquals(ActivityType.ERROR_END_EVENT, errorEndEvent.type());
    }

    @Test
    @DisplayName("Should return empty map for inputs")
    void shouldReturnEmptyMapForInputs() {
        // Given
        var errorEndEvent = ErrorEndEvent.builder()
                .id(TEST_ID)
                .build();

        // When
        var inputs = errorEndEvent.inputs();

        // Then
        assertNotNull(inputs);
        assertTrue(inputs.isEmpty());
    }

    @Test
    @DisplayName("Should return empty map for outputs")
    void shouldReturnEmptyMapForOutputs() {
        // Given
        var errorEndEvent = ErrorEndEvent.builder()
                .id(TEST_ID)
                .build();

        // When
        var outputs = errorEndEvent.outputs();

        // Then
        assertNotNull(outputs);
        assertTrue(outputs.isEmpty());
    }

    @Test
    @DisplayName("Should implement equals correctly")
    void shouldImplementEqualsCorrectly() {
        // Given
        var errorEndEvent1 = ErrorEndEvent.builder()
                .id(TEST_ID)
                .name(TEST_NAME)
                .errorCode(TEST_ERROR_CODE)
                .build();

        var errorEndEvent2 = ErrorEndEvent.builder()
                .id(TEST_ID)
                .name(TEST_NAME)
                .errorCode(TEST_ERROR_CODE)
                .build();

        var errorEndEvent3 = ErrorEndEvent.builder()
                .id("differentId")
                .name(TEST_NAME)
                .errorCode(TEST_ERROR_CODE)
                .build();

        // When & Then
        assertEquals(errorEndEvent1, errorEndEvent2);
        assertNotEquals(errorEndEvent1, errorEndEvent3);
        assertNotEquals(null, errorEndEvent1);
    }

    @Test
    @DisplayName("Should implement hashCode correctly")
    void shouldImplementHashCodeCorrectly() {
        // Given
        var errorEndEvent1 = ErrorEndEvent.builder()
                .id(TEST_ID)
                .name(TEST_NAME)
                .errorCode(TEST_ERROR_CODE)
                .build();

        var errorEndEvent2 = ErrorEndEvent.builder()
                .id(TEST_ID)
                .name(TEST_NAME)
                .errorCode(TEST_ERROR_CODE)
                .build();

        // When & Then
        assertEquals(errorEndEvent1.hashCode(), errorEndEvent2.hashCode());
    }

    @Test
    @DisplayName("Should implement toString correctly")
    void shouldImplementToStringCorrectly() {
        // Given
        var errorEndEvent = ErrorEndEvent.builder()
                .id(TEST_ID)
                .name(TEST_NAME)
                .errorCode(TEST_ERROR_CODE)
                .build();

        // When
        var toStringResult = errorEndEvent.toString();

        // Then
        assertNotNull(toStringResult);
        assertTrue(toStringResult.contains("ErrorEndEvent"));
        assertTrue(toStringResult.contains(TEST_ID));
        assertTrue(toStringResult.contains(TEST_NAME));
        assertTrue(toStringResult.contains(TEST_ERROR_CODE));
    }

    @Test
    @DisplayName("Should handle different error codes")
    void shouldHandleDifferentErrorCodes() {
        // Given
        var technicalError = ErrorEndEvent.builder()
                .id("tech1")
                .errorCode("TECHNICAL_ERROR")
                .build();

        var businessError = ErrorEndEvent.builder()
                .id("business1")
                .errorCode("BUSINESS_RULE_VIOLATION")
                .build();

        // When & Then
        assertEquals("TECHNICAL_ERROR", technicalError.errorCode());
        assertEquals("BUSINESS_RULE_VIOLATION", businessError.errorCode());
        assertNotEquals(technicalError, businessError);
    }

    @Test
    @DisplayName("Should handle empty error code")
    void shouldHandleEmptyErrorCode() {
        // Given
        var emptyErrorCode = "";

        // When
        var errorEndEvent = ErrorEndEvent.builder()
                .id(TEST_ID)
                .errorCode(emptyErrorCode)
                .build();

        // Then
        assertNotNull(errorEndEvent);
        assertEquals(emptyErrorCode, errorEndEvent.errorCode());
    }

}