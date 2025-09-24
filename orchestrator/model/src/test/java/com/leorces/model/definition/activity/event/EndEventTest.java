package com.leorces.model.definition.activity.event;

import com.leorces.model.definition.activity.ActivityType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("EndEvent Tests")
class EndEventTest {

    private static final String TEST_ID = "end-event-123";
    private static final String TEST_PARENT_ID = "parent-456";
    private static final String TEST_NAME = "Test End Event";

    @Test
    @DisplayName("Should create EndEvent with builder pattern")
    void shouldCreateEndEventWithBuilder() {
        // Given
        var incoming = List.of("incoming1", "incoming2");
        var outgoing = List.<String>of(); // End events typically have no outgoing

        // When
        var endEvent = EndEvent.builder()
                .id(TEST_ID)
                .parentId(TEST_PARENT_ID)
                .name(TEST_NAME)
                .type(ActivityType.END_EVENT)
                .incoming(incoming)
                .outgoing(outgoing)
                .build();

        // Then
        assertNotNull(endEvent);
        assertEquals(TEST_ID, endEvent.id());
        assertEquals(TEST_PARENT_ID, endEvent.parentId());
        assertEquals(TEST_NAME, endEvent.name());
        assertEquals(incoming, endEvent.incoming());
        assertEquals(outgoing, endEvent.outgoing());
    }

    @Test
    @DisplayName("Should create EndEvent with minimal fields")
    void shouldCreateEndEventWithMinimalFields() {
        // Given & When
        var endEvent = EndEvent.builder()
                .id(TEST_ID)
                .name(TEST_NAME)
                .build();

        // Then
        assertNotNull(endEvent);
        assertEquals(TEST_ID, endEvent.id());
        assertEquals(TEST_NAME, endEvent.name());
        assertNull(endEvent.parentId());
        assertNull(endEvent.incoming());
        assertNull(endEvent.outgoing());
    }

    @Test
    @DisplayName("Should always return END_EVENT type")
    void shouldAlwaysReturnEndEventType() {
        // Given
        var endEvent = EndEvent.builder()
                .id(TEST_ID)
                .name(TEST_NAME)
                .type(ActivityType.START_EVENT) // Try to set different type
                .build();

        // When
        var type = endEvent.type();

        // Then
        assertEquals(ActivityType.END_EVENT, type);
    }

    @Test
    @DisplayName("Should return empty inputs map")
    void shouldReturnEmptyInputsMap() {
        // Given
        var endEvent = EndEvent.builder()
                .id(TEST_ID)
                .name(TEST_NAME)
                .build();

        // When
        var inputs = endEvent.inputs();

        // Then
        assertNotNull(inputs);
        assertTrue(inputs.isEmpty());
    }

    @Test
    @DisplayName("Should return empty outputs map")
    void shouldReturnEmptyOutputsMap() {
        // Given
        var endEvent = EndEvent.builder()
                .id(TEST_ID)
                .name(TEST_NAME)
                .build();

        // When
        var outputs = endEvent.outputs();

        // Then
        assertNotNull(outputs);
        assertTrue(outputs.isEmpty());
    }

    @Test
    @DisplayName("Should handle multiple incoming connections")
    void shouldHandleMultipleIncomingConnections() {
        // Given
        var incoming = List.of("task1", "task2", "gateway1", "subprocess1");

        // When
        var endEvent = EndEvent.builder()
                .id(TEST_ID)
                .name(TEST_NAME)
                .incoming(incoming)
                .build();

        // Then
        assertNotNull(endEvent);
        assertEquals(4, endEvent.incoming().size());
        assertEquals("task1", endEvent.incoming().get(0));
        assertEquals("subprocess1", endEvent.incoming().get(3));
    }

    @Test
    @DisplayName("Should handle empty outgoing for end event")
    void shouldHandleEmptyOutgoingForEndEvent() {
        // Given
        var emptyOutgoing = List.<String>of();

        // When
        var endEvent = EndEvent.builder()
                .id(TEST_ID)
                .name(TEST_NAME)
                .incoming(List.of("task1"))
                .outgoing(emptyOutgoing)
                .build();

        // Then
        assertNotNull(endEvent);
        assertEquals(emptyOutgoing, endEvent.outgoing());
        assertTrue(endEvent.outgoing().isEmpty());
    }

    @Test
    @DisplayName("Should support toBuilder functionality")
    void shouldSupportToBuilderFunctionality() {
        // Given
        var originalEndEvent = EndEvent.builder()
                .id(TEST_ID)
                .parentId(TEST_PARENT_ID)
                .name(TEST_NAME)
                .incoming(List.of("original"))
                .build();

        // When
        var modifiedEndEvent = originalEndEvent.toBuilder()
                .name("Modified End Event")
                .incoming(List.of("modified1", "modified2"))
                .build();

        // Then
        assertNotEquals(originalEndEvent, modifiedEndEvent);
        assertEquals(TEST_ID, modifiedEndEvent.id());
        assertEquals(TEST_PARENT_ID, modifiedEndEvent.parentId());
        assertEquals("Modified End Event", modifiedEndEvent.name());
        assertEquals(List.of("modified1", "modified2"), modifiedEndEvent.incoming());
    }

    @Test
    @DisplayName("Should implement equals correctly")
    void shouldImplementEqualsCorrectly() {
        // Given
        var endEvent1 = EndEvent.builder()
                .id(TEST_ID)
                .name(TEST_NAME)
                .incoming(List.of("incoming1"))
                .build();

        var endEvent2 = EndEvent.builder()
                .id(TEST_ID)
                .name(TEST_NAME)
                .incoming(List.of("incoming1"))
                .build();

        var endEvent3 = EndEvent.builder()
                .id("different-id")
                .name(TEST_NAME)
                .incoming(List.of("incoming1"))
                .build();

        // When & Then
        assertEquals(endEvent1, endEvent2);
        assertNotEquals(endEvent1, endEvent3);
        assertNotEquals(null, endEvent1);
    }

    @Test
    @DisplayName("Should implement hashCode correctly")
    void shouldImplementHashCodeCorrectly() {
        // Given
        var endEvent1 = EndEvent.builder()
                .id(TEST_ID)
                .name(TEST_NAME)
                .incoming(List.of("incoming1"))
                .build();

        var endEvent2 = EndEvent.builder()
                .id(TEST_ID)
                .name(TEST_NAME)
                .incoming(List.of("incoming1"))
                .build();

        // When & Then
        assertEquals(endEvent1.hashCode(), endEvent2.hashCode());
    }

    @Test
    @DisplayName("Should implement toString correctly")
    void shouldImplementToStringCorrectly() {
        // Given
        var endEvent = EndEvent.builder()
                .id(TEST_ID)
                .name(TEST_NAME)
                .parentId(TEST_PARENT_ID)
                .build();

        // When
        var toStringResult = endEvent.toString();

        // Then
        assertNotNull(toStringResult);
        assertTrue(toStringResult.contains("EndEvent"));
        assertTrue(toStringResult.contains(TEST_ID));
        assertTrue(toStringResult.contains(TEST_NAME));
        assertTrue(toStringResult.contains(TEST_PARENT_ID));
    }

    @Test
    @DisplayName("Should work as ActivityDefinition implementation")
    void shouldWorkAsActivityDefinitionImplementation() {
        // Given
        var endEvent = EndEvent.builder()
                .id(TEST_ID)
                .name(TEST_NAME)
                .incoming(List.of("previous-task"))
                .outgoing(List.of())
                .build();

        // When & Then - Test ActivityDefinition interface methods
        assertEquals(TEST_ID, endEvent.id());
        assertEquals(TEST_NAME, endEvent.name());
        assertEquals(ActivityType.END_EVENT, endEvent.type());
        assertEquals(1, endEvent.incoming().size());
        assertTrue(endEvent.outgoing().isEmpty());
        assertTrue(endEvent.inputs().isEmpty());
        assertTrue(endEvent.outputs().isEmpty());
    }

    @Test
    @DisplayName("Should handle subprocess context")
    void shouldHandleSubprocessContext() {
        // Given
        var subprocessEndEvent = EndEvent.builder()
                .id("subprocess-end")
                .parentId("subprocess-parent")
                .name("Subprocess End")
                .incoming(List.of("subprocess-task"))
                .build();

        // When & Then
        assertEquals("subprocess-end", subprocessEndEvent.id());
        assertEquals("subprocess-parent", subprocessEndEvent.parentId());
        assertEquals("Subprocess End", subprocessEndEvent.name());
        assertEquals(ActivityType.END_EVENT, subprocessEndEvent.type());
        assertEquals(List.of("subprocess-task"), subprocessEndEvent.incoming());
    }

    @Test
    @DisplayName("Should handle process termination scenarios")
    void shouldHandleProcessTerminationScenarios() {
        // Given
        var normalEndEvent = EndEvent.builder()
                .id("normal-end")
                .name("Normal Process End")
                .incoming(List.of("final-task"))
                .outgoing(List.of())
                .build();

        var multiPathEndEvent = EndEvent.builder()
                .id("multi-path-end")
                .name("Multi-Path End")
                .incoming(List.of("path1", "path2", "path3"))
                .outgoing(List.of())
                .build();

        // When & Then
        assertEquals(1, normalEndEvent.incoming().size());
        assertEquals(3, multiPathEndEvent.incoming().size());
        assertTrue(normalEndEvent.outgoing().isEmpty());
        assertTrue(multiPathEndEvent.outgoing().isEmpty());
        assertEquals(ActivityType.END_EVENT, normalEndEvent.type());
        assertEquals(ActivityType.END_EVENT, multiPathEndEvent.type());
    }
}