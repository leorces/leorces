package com.leorces.model.definition.activity.event;

import com.leorces.model.definition.activity.ActivityType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Terminate End Event Tests")
class TerminateEndEventTest {

    private static final String TEST_ID = "terminateEndEvent1";
    private static final String TEST_PARENT_ID = "subprocess1";
    private static final String TEST_NAME = "Terminate End Event";
    private static final List<String> TEST_INCOMING = List.of("task1", "gateway2");
    private static final List<String> TEST_OUTGOING = List.of();

    @Test
    @DisplayName("Should create TerminateEndEvent with all fields using builder")
    void shouldCreateTerminateEndEventWithAllFields() {
        // When
        var terminateEndEvent = TerminateEndEvent.builder()
                .id(TEST_ID)
                .parentId(TEST_PARENT_ID)
                .name(TEST_NAME)
                .type(ActivityType.TERMINATE_END_EVENT)
                .incoming(TEST_INCOMING)
                .outgoing(TEST_OUTGOING)
                .build();

        // Then
        assertNotNull(terminateEndEvent);
        assertEquals(TEST_ID, terminateEndEvent.id());
        assertEquals(TEST_PARENT_ID, terminateEndEvent.parentId());
        assertEquals(TEST_NAME, terminateEndEvent.name());
        assertEquals(ActivityType.TERMINATE_END_EVENT, terminateEndEvent.type());
        assertEquals(TEST_INCOMING, terminateEndEvent.incoming());
        assertEquals(TEST_OUTGOING, terminateEndEvent.outgoing());
    }

    @Test
    @DisplayName("Should create TerminateEndEvent with null fields")
    void shouldCreateTerminateEndEventWithNullFields() {
        // When
        var terminateEndEvent = TerminateEndEvent.builder()
                .id(TEST_ID)
                .parentId(null)
                .name(null)
                .type(null)
                .incoming(null)
                .outgoing(null)
                .build();

        // Then
        assertNotNull(terminateEndEvent);
        assertEquals(TEST_ID, terminateEndEvent.id());
        assertNull(terminateEndEvent.parentId());
        assertNull(terminateEndEvent.name());
        assertEquals(ActivityType.TERMINATE_END_EVENT, terminateEndEvent.type());
        assertNull(terminateEndEvent.incoming());
        assertNull(terminateEndEvent.outgoing());
    }

    @Test
    @DisplayName("Should create TerminateEndEvent with minimal fields")
    void shouldCreateTerminateEndEventWithMinimalFields() {
        // When
        var terminateEndEvent = TerminateEndEvent.builder()
                .id(TEST_ID)
                .name(TEST_NAME)
                .build();

        // Then
        assertNotNull(terminateEndEvent);
        assertEquals(TEST_ID, terminateEndEvent.id());
        assertEquals(TEST_NAME, terminateEndEvent.name());
        assertEquals(ActivityType.TERMINATE_END_EVENT, terminateEndEvent.type());
        assertNull(terminateEndEvent.parentId());
        assertNull(terminateEndEvent.incoming());
        assertNull(terminateEndEvent.outgoing());
    }

    @Test
    @DisplayName("Should support toBuilder functionality")
    void shouldSupportToBuilderFunctionality() {
        // Given
        var originalEvent = TerminateEndEvent.builder()
                .id(TEST_ID)
                .name(TEST_NAME)
                .build();

        // When
        var modifiedEvent = originalEvent.toBuilder()
                .parentId(TEST_PARENT_ID)
                .incoming(TEST_INCOMING)
                .build();

        // Then
        assertEquals(TEST_ID, modifiedEvent.id());
        assertEquals(TEST_NAME, modifiedEvent.name());
        assertEquals(TEST_PARENT_ID, modifiedEvent.parentId());
        assertEquals(TEST_INCOMING, modifiedEvent.incoming());
        // Original should remain unchanged
        assertNull(originalEvent.parentId());
        assertNull(originalEvent.incoming());
    }

    @Test
    @DisplayName("Should override type to return TERMINATE_END_EVENT")
    void shouldOverrideTypeToReturnTerminateEndEvent() {
        // Given
        var terminateEndEvent = TerminateEndEvent.builder()
                .id(TEST_ID)
                .type(ActivityType.START_EVENT) // Different type in constructor
                .build();

        // When & Then
        assertEquals(ActivityType.TERMINATE_END_EVENT, terminateEndEvent.type());
    }

    @Test
    @DisplayName("Should return empty map for inputs")
    void shouldReturnEmptyMapForInputs() {
        // Given
        var terminateEndEvent = TerminateEndEvent.builder()
                .id(TEST_ID)
                .build();

        // When
        var inputs = terminateEndEvent.inputs();

        // Then
        assertNotNull(inputs);
        assertTrue(inputs.isEmpty());
    }

    @Test
    @DisplayName("Should return empty map for outputs")
    void shouldReturnEmptyMapForOutputs() {
        // Given
        var terminateEndEvent = TerminateEndEvent.builder()
                .id(TEST_ID)
                .build();

        // When
        var outputs = terminateEndEvent.outputs();

        // Then
        assertNotNull(outputs);
        assertTrue(outputs.isEmpty());
    }

    @Test
    @DisplayName("Should implement equals correctly")
    void shouldImplementEqualsCorrectly() {
        // Given
        var terminateEndEvent1 = TerminateEndEvent.builder()
                .id(TEST_ID)
                .name(TEST_NAME)
                .incoming(TEST_INCOMING)
                .build();

        var terminateEndEvent2 = TerminateEndEvent.builder()
                .id(TEST_ID)
                .name(TEST_NAME)
                .incoming(TEST_INCOMING)
                .build();

        var terminateEndEvent3 = TerminateEndEvent.builder()
                .id("differentId")
                .name(TEST_NAME)
                .incoming(TEST_INCOMING)
                .build();

        // When & Then
        assertEquals(terminateEndEvent1, terminateEndEvent2);
        assertNotEquals(terminateEndEvent1, terminateEndEvent3);
        assertNotEquals(null, terminateEndEvent1);
    }

    @Test
    @DisplayName("Should implement hashCode correctly")
    void shouldImplementHashCodeCorrectly() {
        // Given
        var terminateEndEvent1 = TerminateEndEvent.builder()
                .id(TEST_ID)
                .name(TEST_NAME)
                .incoming(TEST_INCOMING)
                .build();

        var terminateEndEvent2 = TerminateEndEvent.builder()
                .id(TEST_ID)
                .name(TEST_NAME)
                .incoming(TEST_INCOMING)
                .build();

        // When & Then
        assertEquals(terminateEndEvent1.hashCode(), terminateEndEvent2.hashCode());
    }

    @Test
    @DisplayName("Should implement toString correctly")
    void shouldImplementToStringCorrectly() {
        // Given
        var terminateEndEvent = TerminateEndEvent.builder()
                .id(TEST_ID)
                .name(TEST_NAME)
                .build();

        // When
        var toStringResult = terminateEndEvent.toString();

        // Then
        assertNotNull(toStringResult);
        assertTrue(toStringResult.contains("TerminateEndEvent"));
        assertTrue(toStringResult.contains(TEST_ID));
        assertTrue(toStringResult.contains(TEST_NAME));
    }

    @Test
    @DisplayName("Should handle empty lists for incoming and outgoing")
    void shouldHandleEmptyListsForIncomingAndOutgoing() {
        // Given
        var emptyIncoming = List.<String>of();
        var emptyOutgoing = List.<String>of();

        // When
        var terminateEndEvent = TerminateEndEvent.builder()
                .id(TEST_ID)
                .incoming(emptyIncoming)
                .outgoing(emptyOutgoing)
                .build();

        // Then
        assertNotNull(terminateEndEvent);
        assertEquals(emptyIncoming, terminateEndEvent.incoming());
        assertEquals(emptyOutgoing, terminateEndEvent.outgoing());
        assertTrue(terminateEndEvent.incoming().isEmpty());
        assertTrue(terminateEndEvent.outgoing().isEmpty());
    }

    @Test
    @DisplayName("Should work as ActivityDefinition interface")
    void shouldWorkAsActivityDefinitionInterface() {
        // Given
        var terminateEndEvent = TerminateEndEvent.builder()
                .id(TEST_ID)
                .parentId(TEST_PARENT_ID)
                .name(TEST_NAME)
                .incoming(TEST_INCOMING)
                .outgoing(TEST_OUTGOING)
                .build();

        // When - casting to interface
        var activityDefinition = (com.leorces.model.definition.activity.ActivityDefinition) terminateEndEvent;

        // Then
        assertEquals(TEST_ID, activityDefinition.id());
        assertEquals(TEST_PARENT_ID, activityDefinition.parentId());
        assertEquals(TEST_NAME, activityDefinition.name());
        assertEquals(ActivityType.TERMINATE_END_EVENT, activityDefinition.type());
        assertEquals(TEST_INCOMING, activityDefinition.incoming());
        assertEquals(TEST_OUTGOING, activityDefinition.outgoing());
        assertTrue(activityDefinition.inputs().isEmpty());
        assertTrue(activityDefinition.outputs().isEmpty());
    }

}