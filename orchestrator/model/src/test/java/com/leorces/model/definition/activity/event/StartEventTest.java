package com.leorces.model.definition.activity.event;

import com.leorces.model.definition.activity.ActivityType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("StartEvent Tests")
class StartEventTest {

    private static final String TEST_ID = "start-event-123";
    private static final String TEST_PARENT_ID = "parent-456";
    private static final String TEST_NAME = "Test Start Event";

    @Test
    @DisplayName("Should create StartEvent with builder pattern")
    void shouldCreateStartEventWithBuilder() {
        // Given
        var incoming = List.of("incoming1");
        var outgoing = List.of("outgoing1", "outgoing2");

        // When
        var startEvent = StartEvent.builder()
                .id(TEST_ID)
                .parentId(TEST_PARENT_ID)
                .name(TEST_NAME)
                .type(ActivityType.START_EVENT)
                .incoming(incoming)
                .outgoing(outgoing)
                .build();

        // Then
        assertNotNull(startEvent);
        assertEquals(TEST_ID, startEvent.id());
        assertEquals(TEST_PARENT_ID, startEvent.parentId());
        assertEquals(TEST_NAME, startEvent.name());
        assertEquals(incoming, startEvent.incoming());
        assertEquals(outgoing, startEvent.outgoing());
    }

    @Test
    @DisplayName("Should create StartEvent with minimal fields")
    void shouldCreateStartEventWithMinimalFields() {
        // Given & When
        var startEvent = StartEvent.builder()
                .id(TEST_ID)
                .name(TEST_NAME)
                .build();

        // Then
        assertNotNull(startEvent);
        assertEquals(TEST_ID, startEvent.id());
        assertEquals(TEST_NAME, startEvent.name());
        assertNull(startEvent.parentId());
        assertNull(startEvent.incoming());
        assertNull(startEvent.outgoing());
    }

    @Test
    @DisplayName("Should always return START_EVENT type")
    void shouldAlwaysReturnStartEventType() {
        // Given
        var startEvent = StartEvent.builder()
                .id(TEST_ID)
                .name(TEST_NAME)
                .type(ActivityType.END_EVENT) // Try to set different type
                .build();

        // When
        var type = startEvent.type();

        // Then
        assertEquals(ActivityType.START_EVENT, type);
    }

    @Test
    @DisplayName("Should return empty inputs map")
    void shouldReturnEmptyInputsMap() {
        // Given
        var startEvent = StartEvent.builder()
                .id(TEST_ID)
                .name(TEST_NAME)
                .build();

        // When
        var inputs = startEvent.inputs();

        // Then
        assertNotNull(inputs);
        assertTrue(inputs.isEmpty());
    }

    @Test
    @DisplayName("Should return empty outputs map")
    void shouldReturnEmptyOutputsMap() {
        // Given
        var startEvent = StartEvent.builder()
                .id(TEST_ID)
                .name(TEST_NAME)
                .build();

        // When
        var outputs = startEvent.outputs();

        // Then
        assertNotNull(outputs);
        assertTrue(outputs.isEmpty());
    }

    @Test
    @DisplayName("Should handle empty collections")
    void shouldHandleEmptyCollections() {
        // Given
        var emptyIncoming = List.<String>of();
        var emptyOutgoing = List.<String>of();

        // When
        var startEvent = StartEvent.builder()
                .id(TEST_ID)
                .name(TEST_NAME)
                .incoming(emptyIncoming)
                .outgoing(emptyOutgoing)
                .build();

        // Then
        assertNotNull(startEvent);
        assertEquals(emptyIncoming, startEvent.incoming());
        assertEquals(emptyOutgoing, startEvent.outgoing());
        assertTrue(startEvent.incoming().isEmpty());
        assertTrue(startEvent.outgoing().isEmpty());
    }

    @Test
    @DisplayName("Should handle multiple outgoing connections")
    void shouldHandleMultipleOutgoingConnections() {
        // Given
        var outgoing = List.of("task1", "task2", "gateway1", "end1");

        // When
        var startEvent = StartEvent.builder()
                .id(TEST_ID)
                .name(TEST_NAME)
                .outgoing(outgoing)
                .build();

        // Then
        assertNotNull(startEvent);
        assertEquals(4, startEvent.outgoing().size());
        assertEquals("task1", startEvent.outgoing().get(0));
        assertEquals("end1", startEvent.outgoing().get(3));
    }

    @Test
    @DisplayName("Should support toBuilder functionality")
    void shouldSupportToBuilderFunctionality() {
        // Given
        var originalStartEvent = StartEvent.builder()
                .id(TEST_ID)
                .parentId(TEST_PARENT_ID)
                .name(TEST_NAME)
                .outgoing(List.of("original"))
                .build();

        // When
        var modifiedStartEvent = originalStartEvent.toBuilder()
                .name("Modified Start Event")
                .outgoing(List.of("modified1", "modified2"))
                .build();

        // Then
        assertNotEquals(originalStartEvent, modifiedStartEvent);
        assertEquals(TEST_ID, modifiedStartEvent.id());
        assertEquals(TEST_PARENT_ID, modifiedStartEvent.parentId());
        assertEquals("Modified Start Event", modifiedStartEvent.name());
        assertEquals(List.of("modified1", "modified2"), modifiedStartEvent.outgoing());
    }

    @Test
    @DisplayName("Should implement equals correctly")
    void shouldImplementEqualsCorrectly() {
        // Given
        var startEvent1 = StartEvent.builder()
                .id(TEST_ID)
                .name(TEST_NAME)
                .outgoing(List.of("outgoing1"))
                .build();

        var startEvent2 = StartEvent.builder()
                .id(TEST_ID)
                .name(TEST_NAME)
                .outgoing(List.of("outgoing1"))
                .build();

        var startEvent3 = StartEvent.builder()
                .id("different-id")
                .name(TEST_NAME)
                .outgoing(List.of("outgoing1"))
                .build();

        // When & Then
        assertEquals(startEvent1, startEvent2);
        assertNotEquals(startEvent1, startEvent3);
        assertNotEquals(null, startEvent1);
    }

    @Test
    @DisplayName("Should implement hashCode correctly")
    void shouldImplementHashCodeCorrectly() {
        // Given
        var startEvent1 = StartEvent.builder()
                .id(TEST_ID)
                .name(TEST_NAME)
                .outgoing(List.of("outgoing1"))
                .build();

        var startEvent2 = StartEvent.builder()
                .id(TEST_ID)
                .name(TEST_NAME)
                .outgoing(List.of("outgoing1"))
                .build();

        // When & Then
        assertEquals(startEvent1.hashCode(), startEvent2.hashCode());
    }

    @Test
    @DisplayName("Should implement toString correctly")
    void shouldImplementToStringCorrectly() {
        // Given
        var startEvent = StartEvent.builder()
                .id(TEST_ID)
                .name(TEST_NAME)
                .parentId(TEST_PARENT_ID)
                .build();

        // When
        var toStringResult = startEvent.toString();

        // Then
        assertNotNull(toStringResult);
        assertTrue(toStringResult.contains("StartEvent"));
        assertTrue(toStringResult.contains(TEST_ID));
        assertTrue(toStringResult.contains(TEST_NAME));
        assertTrue(toStringResult.contains(TEST_PARENT_ID));
    }

    @Test
    @DisplayName("Should work as ActivityDefinition implementation")
    void shouldWorkAsActivityDefinitionImplementation() {
        // Given
        var startEvent = StartEvent.builder()
                .id(TEST_ID)
                .name(TEST_NAME)
                .incoming(List.of())
                .outgoing(List.of("next-task"))
                .build();

        // When & Then - Test ActivityDefinition interface methods
        assertEquals(TEST_ID, startEvent.id());
        assertEquals(TEST_NAME, startEvent.name());
        assertEquals(ActivityType.START_EVENT, startEvent.type());
        assertTrue(startEvent.incoming().isEmpty());
        assertEquals(1, startEvent.outgoing().size());
        assertTrue(startEvent.inputs().isEmpty());
        assertTrue(startEvent.outputs().isEmpty());
    }

    @Test
    @DisplayName("Should handle subprocess context")
    void shouldHandleSubprocessContext() {
        // Given
        var subprocessStartEvent = StartEvent.builder()
                .id("subprocess-start")
                .parentId("subprocess-parent")
                .name("Subprocess Start")
                .outgoing(List.of("subprocess-task"))
                .build();

        // When & Then
        assertEquals("subprocess-start", subprocessStartEvent.id());
        assertEquals("subprocess-parent", subprocessStartEvent.parentId());
        assertEquals("Subprocess Start", subprocessStartEvent.name());
        assertEquals(ActivityType.START_EVENT, subprocessStartEvent.type());
        assertEquals(List.of("subprocess-task"), subprocessStartEvent.outgoing());
    }

}