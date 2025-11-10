package com.leorces.model.definition.activity.event.start;

import com.leorces.model.definition.activity.ActivityType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Error Start Event Tests")
class ErrorStartEventTest {

    private static final String TEST_ID = "errorStartEvent1";
    private static final String TEST_PARENT_ID = "subprocess1";
    private static final String TEST_NAME = "Error Start Event";
    private static final List<String> TEST_INCOMING = List.of();
    private static final List<String> TEST_OUTGOING = List.of("task1", "gateway2");
    private static final String TEST_ERROR_CODE = "VALIDATION_ERROR";

    @Test
    @DisplayName("Should create ErrorStartEvent with all fields using builder")
    void shouldCreateErrorStartEventWithAllFields() {
        // When
        var errorStartEvent = ErrorStartEvent.builder()
                .id(TEST_ID)
                .parentId(TEST_PARENT_ID)
                .name(TEST_NAME)
                .type(ActivityType.ERROR_START_EVENT)
                .incoming(TEST_INCOMING)
                .outgoing(TEST_OUTGOING)
                .errorCode(TEST_ERROR_CODE)
                .build();

        // Then
        assertNotNull(errorStartEvent);
        assertEquals(TEST_ID, errorStartEvent.id());
        assertEquals(TEST_PARENT_ID, errorStartEvent.parentId());
        assertEquals(TEST_NAME, errorStartEvent.name());
        assertEquals(ActivityType.ERROR_START_EVENT, errorStartEvent.type());
        assertEquals(TEST_INCOMING, errorStartEvent.incoming());
        assertEquals(TEST_OUTGOING, errorStartEvent.outgoing());
        assertEquals(TEST_ERROR_CODE, errorStartEvent.errorCode());
    }

    @Test
    @DisplayName("Should create ErrorStartEvent with null fields")
    void shouldCreateErrorStartEventWithNullFields() {
        // When
        var errorStartEvent = ErrorStartEvent.builder()
                .id(TEST_ID)
                .parentId(null)
                .name(null)
                .type(null)
                .incoming(null)
                .outgoing(null)
                .errorCode(null)
                .build();

        // Then
        assertNotNull(errorStartEvent);
        assertEquals(TEST_ID, errorStartEvent.id());
        assertNull(errorStartEvent.parentId());
        assertNull(errorStartEvent.name());
        assertEquals(ActivityType.ERROR_START_EVENT, errorStartEvent.type());
        assertNull(errorStartEvent.incoming());
        assertNull(errorStartEvent.outgoing());
        assertNull(errorStartEvent.errorCode());
    }

    @Test
    @DisplayName("Should create ErrorStartEvent with minimal fields")
    void shouldCreateErrorStartEventWithMinimalFields() {
        // When
        var errorStartEvent = ErrorStartEvent.builder()
                .id(TEST_ID)
                .name(TEST_NAME)
                .errorCode(TEST_ERROR_CODE)
                .build();

        // Then
        assertNotNull(errorStartEvent);
        assertEquals(TEST_ID, errorStartEvent.id());
        assertEquals(TEST_NAME, errorStartEvent.name());
        assertEquals(TEST_ERROR_CODE, errorStartEvent.errorCode());
        assertEquals(ActivityType.ERROR_START_EVENT, errorStartEvent.type());
        assertNull(errorStartEvent.parentId());
        assertNull(errorStartEvent.incoming());
        assertNull(errorStartEvent.outgoing());
    }

    @Test
    @DisplayName("Should support toBuilder functionality")
    void shouldSupportToBuilderFunctionality() {
        // Given
        var originalEvent = ErrorStartEvent.builder()
                .id(TEST_ID)
                .name(TEST_NAME)
                .errorCode(TEST_ERROR_CODE)
                .build();

        // When
        var modifiedEvent = originalEvent.toBuilder()
                .parentId(TEST_PARENT_ID)
                .outgoing(TEST_OUTGOING)
                .build();

        // Then
        assertEquals(TEST_ID, modifiedEvent.id());
        assertEquals(TEST_NAME, modifiedEvent.name());
        assertEquals(TEST_ERROR_CODE, modifiedEvent.errorCode());
        assertEquals(TEST_PARENT_ID, modifiedEvent.parentId());
        assertEquals(TEST_OUTGOING, modifiedEvent.outgoing());
        // Original should remain unchanged
        assertNull(originalEvent.parentId());
        assertNull(originalEvent.outgoing());
    }

    @Test
    @DisplayName("Should override type to return ERROR_START_EVENT")
    void shouldOverrideTypeToReturnErrorStartEvent() {
        // Given
        var errorStartEvent = ErrorStartEvent.builder()
                .id(TEST_ID)
                .type(ActivityType.END_EVENT) // Different type in constructor
                .build();

        // When & Then
        assertEquals(ActivityType.ERROR_START_EVENT, errorStartEvent.type());
    }

    @Test
    @DisplayName("Should return empty map for inputs")
    void shouldReturnEmptyMapForInputs() {
        // Given
        var errorStartEvent = ErrorStartEvent.builder()
                .id(TEST_ID)
                .build();

        // When
        var inputs = errorStartEvent.inputs();

        // Then
        assertNotNull(inputs);
        assertTrue(inputs.isEmpty());
    }

    @Test
    @DisplayName("Should return empty map for outputs")
    void shouldReturnEmptyMapForOutputs() {
        // Given
        var errorStartEvent = ErrorStartEvent.builder()
                .id(TEST_ID)
                .build();

        // When
        var outputs = errorStartEvent.outputs();

        // Then
        assertNotNull(outputs);
        assertTrue(outputs.isEmpty());
    }

    @Test
    @DisplayName("Should implement equals correctly")
    void shouldImplementEqualsCorrectly() {
        // Given
        var errorStartEvent1 = ErrorStartEvent.builder()
                .id(TEST_ID)
                .name(TEST_NAME)
                .errorCode(TEST_ERROR_CODE)
                .build();

        var errorStartEvent2 = ErrorStartEvent.builder()
                .id(TEST_ID)
                .name(TEST_NAME)
                .errorCode(TEST_ERROR_CODE)
                .build();

        var errorStartEvent3 = ErrorStartEvent.builder()
                .id("differentId")
                .name(TEST_NAME)
                .errorCode(TEST_ERROR_CODE)
                .build();

        // When & Then
        assertEquals(errorStartEvent1, errorStartEvent2);
        assertNotEquals(errorStartEvent1, errorStartEvent3);
        assertNotEquals(null, errorStartEvent1);
    }

    @Test
    @DisplayName("Should implement hashCode correctly")
    void shouldImplementHashCodeCorrectly() {
        // Given
        var errorStartEvent1 = ErrorStartEvent.builder()
                .id(TEST_ID)
                .name(TEST_NAME)
                .errorCode(TEST_ERROR_CODE)
                .build();

        var errorStartEvent2 = ErrorStartEvent.builder()
                .id(TEST_ID)
                .name(TEST_NAME)
                .errorCode(TEST_ERROR_CODE)
                .build();

        // When & Then
        assertEquals(errorStartEvent1.hashCode(), errorStartEvent2.hashCode());
    }

    @Test
    @DisplayName("Should implement toString correctly")
    void shouldImplementToStringCorrectly() {
        // Given
        var errorStartEvent = ErrorStartEvent.builder()
                .id(TEST_ID)
                .name(TEST_NAME)
                .errorCode(TEST_ERROR_CODE)
                .build();

        // When
        var toStringResult = errorStartEvent.toString();

        // Then
        assertNotNull(toStringResult);
        assertTrue(toStringResult.contains("ErrorStartEvent"));
        assertTrue(toStringResult.contains(TEST_ID));
        assertTrue(toStringResult.contains(TEST_NAME));
        assertTrue(toStringResult.contains(TEST_ERROR_CODE));
    }

    @Test
    @DisplayName("Should handle different error codes")
    void shouldHandleDifferentErrorCodes() {
        // Given
        var systemError = ErrorStartEvent.builder()
                .id("system1")
                .errorCode("SYSTEM_FAILURE")
                .build();

        var timeoutError = ErrorStartEvent.builder()
                .id("timeout1")
                .errorCode("TIMEOUT_ERROR")
                .build();

        // When & Then
        assertEquals("SYSTEM_FAILURE", systemError.errorCode());
        assertEquals("TIMEOUT_ERROR", timeoutError.errorCode());
        assertNotEquals(systemError, timeoutError);
    }

    @Test
    @DisplayName("Should handle empty error code")
    void shouldHandleEmptyErrorCode() {
        // Given
        var emptyErrorCode = "";

        // When
        var errorStartEvent = ErrorStartEvent.builder()
                .id(TEST_ID)
                .errorCode(emptyErrorCode)
                .build();

        // Then
        assertNotNull(errorStartEvent);
        assertEquals(emptyErrorCode, errorStartEvent.errorCode());
    }

    @Test
    @DisplayName("Should work as ErrorActivityDefinition interface")
    void shouldWorkAsErrorActivityDefinitionInterface() {
        // Given
        var errorStartEvent = ErrorStartEvent.builder()
                .id(TEST_ID)
                .parentId(TEST_PARENT_ID)
                .name(TEST_NAME)
                .outgoing(TEST_OUTGOING)
                .errorCode(TEST_ERROR_CODE)
                .build();

        // When - casting to interface
        var errorActivityDefinition = (com.leorces.model.definition.activity.ErrorActivityDefinition) errorStartEvent;

        // Then
        assertEquals(TEST_ID, errorActivityDefinition.id());
        assertEquals(TEST_PARENT_ID, errorActivityDefinition.parentId());
        assertEquals(TEST_NAME, errorActivityDefinition.name());
        assertEquals(ActivityType.ERROR_START_EVENT, errorActivityDefinition.type());
        assertEquals(TEST_OUTGOING, errorActivityDefinition.outgoing());
        assertEquals(TEST_ERROR_CODE, errorActivityDefinition.errorCode());
        assertTrue(errorActivityDefinition.inputs().isEmpty());
        assertTrue(errorActivityDefinition.outputs().isEmpty());
    }

    @Test
    @DisplayName("Should handle typical start event incoming connections")
    void shouldHandleTypicalStartEventIncomingConnections() {
        // Given - Start events typically have empty incoming connections
        var emptyIncoming = List.<String>of();

        // When
        var errorStartEvent = ErrorStartEvent.builder()
                .id(TEST_ID)
                .incoming(emptyIncoming)
                .outgoing(TEST_OUTGOING)
                .build();

        // Then
        assertNotNull(errorStartEvent);
        assertEquals(emptyIncoming, errorStartEvent.incoming());
        assertTrue(errorStartEvent.incoming().isEmpty());
        assertEquals(TEST_OUTGOING, errorStartEvent.outgoing());
        assertFalse(errorStartEvent.outgoing().isEmpty());
    }

}