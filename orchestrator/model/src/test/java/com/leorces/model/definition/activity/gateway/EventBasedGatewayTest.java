package com.leorces.model.definition.activity.gateway;

import com.leorces.model.definition.activity.ActivityType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("EventBasedGateway Tests")
class EventBasedGatewayTest {

    private static final String TEST_ID = "event-gateway-123";
    private static final String TEST_PARENT_ID = "parent-456";
    private static final String TEST_NAME = "Test Event-Based Gateway";

    @Test
    @DisplayName("Should create EventBasedGateway with builder pattern")
    void shouldCreateEventBasedGatewayWithBuilder() {
        // Given
        var incoming = List.of("incoming1", "incoming2");
        var outgoing = List.of("outgoing1", "outgoing2", "outgoing3");

        // When
        var eventGateway = EventBasedGateway.builder()
                .id(TEST_ID)
                .parentId(TEST_PARENT_ID)
                .name(TEST_NAME)
                .type(ActivityType.EVENT_BASED_GATEWAY)
                .incoming(incoming)
                .outgoing(outgoing)
                .build();

        // Then
        assertNotNull(eventGateway);
        assertEquals(TEST_ID, eventGateway.id());
        assertEquals(TEST_PARENT_ID, eventGateway.parentId());
        assertEquals(TEST_NAME, eventGateway.name());
        assertEquals(incoming, eventGateway.incoming());
        assertEquals(outgoing, eventGateway.outgoing());
    }

    @Test
    @DisplayName("Should create EventBasedGateway with minimal fields")
    void shouldCreateEventBasedGatewayWithMinimalFields() {
        // Given & When
        var eventGateway = EventBasedGateway.builder()
                .id(TEST_ID)
                .name(TEST_NAME)
                .build();

        // Then
        assertNotNull(eventGateway);
        assertEquals(TEST_ID, eventGateway.id());
        assertEquals(TEST_NAME, eventGateway.name());
        assertNull(eventGateway.parentId());
        assertNull(eventGateway.incoming());
        assertNull(eventGateway.outgoing());
    }

    @Test
    @DisplayName("Should always return EVENT_BASED_GATEWAY type")
    void shouldAlwaysReturnEventBasedGatewayType() {
        // Given
        var eventGateway = EventBasedGateway.builder()
                .id(TEST_ID)
                .name(TEST_NAME)
                .type(ActivityType.PARALLEL_GATEWAY) // Try to set different type
                .build();

        // When
        var type = eventGateway.type();

        // Then
        assertEquals(ActivityType.EVENT_BASED_GATEWAY, type);
    }

    @Test
    @DisplayName("Should return empty inputs map")
    void shouldReturnEmptyInputsMap() {
        // Given
        var eventGateway = EventBasedGateway.builder()
                .id(TEST_ID)
                .name(TEST_NAME)
                .build();

        // When
        var inputs = eventGateway.inputs();

        // Then
        assertNotNull(inputs);
        assertTrue(inputs.isEmpty());
    }

    @Test
    @DisplayName("Should return empty outputs map")
    void shouldReturnEmptyOutputsMap() {
        // Given
        var eventGateway = EventBasedGateway.builder()
                .id(TEST_ID)
                .name(TEST_NAME)
                .build();

        // When
        var outputs = eventGateway.outputs();

        // Then
        assertNotNull(outputs);
        assertTrue(outputs.isEmpty());
    }

    @Test
    @DisplayName("Should handle multiple incoming and outgoing connections")
    void shouldHandleMultipleIncomingAndOutgoingConnections() {
        // Given
        var incoming = List.of("task1", "task2", "gateway1");
        var outgoing = List.of("event1", "event2", "event3", "timeout-event");

        // When
        var eventGateway = EventBasedGateway.builder()
                .id(TEST_ID)
                .name(TEST_NAME)
                .incoming(incoming)
                .outgoing(outgoing)
                .build();

        // Then
        assertNotNull(eventGateway);
        assertEquals(3, eventGateway.incoming().size());
        assertEquals(4, eventGateway.outgoing().size());
        assertEquals("task1", eventGateway.incoming().get(0));
        assertEquals("timeout-event", eventGateway.outgoing().get(3));
    }

    @Test
    @DisplayName("Should handle empty collections")
    void shouldHandleEmptyCollections() {
        // Given
        var emptyIncoming = List.<String>of();
        var emptyOutgoing = List.<String>of();

        // When
        var eventGateway = EventBasedGateway.builder()
                .id(TEST_ID)
                .name(TEST_NAME)
                .incoming(emptyIncoming)
                .outgoing(emptyOutgoing)
                .build();

        // Then
        assertNotNull(eventGateway);
        assertEquals(emptyIncoming, eventGateway.incoming());
        assertEquals(emptyOutgoing, eventGateway.outgoing());
        assertTrue(eventGateway.incoming().isEmpty());
        assertTrue(eventGateway.outgoing().isEmpty());
    }

    @Test
    @DisplayName("Should support toBuilder functionality")
    void shouldSupportToBuilderFunctionality() {
        // Given
        var originalGateway = EventBasedGateway.builder()
                .id(TEST_ID)
                .parentId(TEST_PARENT_ID)
                .name(TEST_NAME)
                .incoming(List.of("original"))
                .outgoing(List.of("original-out"))
                .build();

        // When
        var modifiedGateway = originalGateway.toBuilder()
                .name("Modified Event Gateway")
                .incoming(List.of("modified1", "modified2"))
                .outgoing(List.of("modified-out1", "modified-out2"))
                .build();

        // Then
        assertNotEquals(originalGateway, modifiedGateway);
        assertEquals(TEST_ID, modifiedGateway.id());
        assertEquals(TEST_PARENT_ID, modifiedGateway.parentId());
        assertEquals("Modified Event Gateway", modifiedGateway.name());
        assertEquals(List.of("modified1", "modified2"), modifiedGateway.incoming());
        assertEquals(List.of("modified-out1", "modified-out2"), modifiedGateway.outgoing());
    }

    @Test
    @DisplayName("Should implement equals correctly")
    void shouldImplementEqualsCorrectly() {
        // Given
        var gateway1 = EventBasedGateway.builder()
                .id(TEST_ID)
                .name(TEST_NAME)
                .incoming(List.of("incoming1"))
                .outgoing(List.of("outgoing1"))
                .build();

        var gateway2 = EventBasedGateway.builder()
                .id(TEST_ID)
                .name(TEST_NAME)
                .incoming(List.of("incoming1"))
                .outgoing(List.of("outgoing1"))
                .build();

        var gateway3 = EventBasedGateway.builder()
                .id("different-id")
                .name(TEST_NAME)
                .incoming(List.of("incoming1"))
                .outgoing(List.of("outgoing1"))
                .build();

        // When & Then
        assertEquals(gateway1, gateway2);
        assertNotEquals(gateway1, gateway3);
        assertNotEquals(null, gateway1);
    }

    @Test
    @DisplayName("Should implement hashCode correctly")
    void shouldImplementHashCodeCorrectly() {
        // Given
        var gateway1 = EventBasedGateway.builder()
                .id(TEST_ID)
                .name(TEST_NAME)
                .incoming(List.of("incoming1"))
                .outgoing(List.of("outgoing1"))
                .build();

        var gateway2 = EventBasedGateway.builder()
                .id(TEST_ID)
                .name(TEST_NAME)
                .incoming(List.of("incoming1"))
                .outgoing(List.of("outgoing1"))
                .build();

        // When & Then
        assertEquals(gateway1.hashCode(), gateway2.hashCode());
    }

    @Test
    @DisplayName("Should implement toString correctly")
    void shouldImplementToStringCorrectly() {
        // Given
        var eventGateway = EventBasedGateway.builder()
                .id(TEST_ID)
                .name(TEST_NAME)
                .parentId(TEST_PARENT_ID)
                .build();

        // When
        var toStringResult = eventGateway.toString();

        // Then
        assertNotNull(toStringResult);
        assertTrue(toStringResult.contains("EventBasedGateway"));
        assertTrue(toStringResult.contains(TEST_ID));
        assertTrue(toStringResult.contains(TEST_NAME));
        assertTrue(toStringResult.contains(TEST_PARENT_ID));
    }

    @Test
    @DisplayName("Should work as ActivityDefinition implementation")
    void shouldWorkAsActivityDefinitionImplementation() {
        // Given
        var eventGateway = EventBasedGateway.builder()
                .id(TEST_ID)
                .name(TEST_NAME)
                .incoming(List.of("previous-task"))
                .outgoing(List.of("event1", "event2"))
                .build();

        // When & Then - Test ActivityDefinition interface methods
        assertEquals(TEST_ID, eventGateway.id());
        assertEquals(TEST_NAME, eventGateway.name());
        assertEquals(ActivityType.EVENT_BASED_GATEWAY, eventGateway.type());
        assertEquals(1, eventGateway.incoming().size());
        assertEquals(2, eventGateway.outgoing().size());
        assertTrue(eventGateway.inputs().isEmpty());
        assertTrue(eventGateway.outputs().isEmpty());
    }

    @Test
    @DisplayName("Should handle event-based gateway scenarios")
    void shouldHandleEventBasedGatewayScenarios() {
        // Given
        var raceConditionGateway = EventBasedGateway.builder()
                .id("race-gateway")
                .name("Race Condition Gateway")
                .incoming(List.of("start-task"))
                .outgoing(List.of("message-event", "timer-event", "signal-event"))
                .build();

        var eventChoiceGateway = EventBasedGateway.builder()
                .id("choice-gateway")
                .name("Event Choice Gateway")
                .incoming(List.of("decision-task"))
                .outgoing(List.of("approval-event", "rejection-event", "timeout-event"))
                .build();

        // When & Then
        assertEquals(ActivityType.EVENT_BASED_GATEWAY, raceConditionGateway.type());
        assertEquals(ActivityType.EVENT_BASED_GATEWAY, eventChoiceGateway.type());
        assertEquals(3, raceConditionGateway.outgoing().size());
        assertEquals(3, eventChoiceGateway.outgoing().size());
        assertTrue(raceConditionGateway.outgoing().contains("timer-event"));
        assertTrue(eventChoiceGateway.outgoing().contains("timeout-event"));
    }

    @Test
    @DisplayName("Should handle subprocess context")
    void shouldHandleSubprocessContext() {
        // Given
        var subprocessGateway = EventBasedGateway.builder()
                .id("subprocess-event-gateway")
                .parentId("subprocess-parent")
                .name("Subprocess Event Gateway")
                .incoming(List.of("subprocess-task"))
                .outgoing(List.of("subprocess-event1", "subprocess-event2"))
                .build();

        // When & Then
        assertEquals("subprocess-event-gateway", subprocessGateway.id());
        assertEquals("subprocess-parent", subprocessGateway.parentId());
        assertEquals("Subprocess Event Gateway", subprocessGateway.name());
        assertEquals(ActivityType.EVENT_BASED_GATEWAY, subprocessGateway.type());
        assertEquals(List.of("subprocess-task"), subprocessGateway.incoming());
        assertEquals(List.of("subprocess-event1", "subprocess-event2"), subprocessGateway.outgoing());
    }
}