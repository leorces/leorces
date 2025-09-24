package com.leorces.model.definition.activity.gateway;

import com.leorces.model.definition.activity.ActivityType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Parallel Gateway Tests")
class ParallelGatewayTest {

    private static final String TEST_ID = "parallelGateway1";
    private static final String TEST_PARENT_ID = "subprocess1";
    private static final String TEST_NAME = "Parallel Gateway";
    private static final List<String> TEST_INCOMING = List.of("task1", "task2");
    private static final List<String> TEST_OUTGOING = List.of("task3", "task4", "task5");

    @Test
    @DisplayName("Should create ParallelGateway with all fields using builder")
    void shouldCreateParallelGatewayWithAllFields() {
        // When
        var parallelGateway = ParallelGateway.builder()
                .id(TEST_ID)
                .parentId(TEST_PARENT_ID)
                .name(TEST_NAME)
                .type(ActivityType.PARALLEL_GATEWAY)
                .incoming(TEST_INCOMING)
                .outgoing(TEST_OUTGOING)
                .build();

        // Then
        assertNotNull(parallelGateway);
        assertEquals(TEST_ID, parallelGateway.id());
        assertEquals(TEST_PARENT_ID, parallelGateway.parentId());
        assertEquals(TEST_NAME, parallelGateway.name());
        assertEquals(ActivityType.PARALLEL_GATEWAY, parallelGateway.type());
        assertEquals(TEST_INCOMING, parallelGateway.incoming());
        assertEquals(TEST_OUTGOING, parallelGateway.outgoing());
    }

    @Test
    @DisplayName("Should create ParallelGateway with null fields")
    void shouldCreateParallelGatewayWithNullFields() {
        // When
        var parallelGateway = ParallelGateway.builder()
                .id(TEST_ID)
                .parentId(null)
                .name(null)
                .type(null)
                .incoming(null)
                .outgoing(null)
                .build();

        // Then
        assertNotNull(parallelGateway);
        assertEquals(TEST_ID, parallelGateway.id());
        assertNull(parallelGateway.parentId());
        assertNull(parallelGateway.name());
        assertEquals(ActivityType.PARALLEL_GATEWAY, parallelGateway.type());
        assertNull(parallelGateway.incoming());
        assertNull(parallelGateway.outgoing());
    }

    @Test
    @DisplayName("Should create ParallelGateway with minimal fields")
    void shouldCreateParallelGatewayWithMinimalFields() {
        // When
        var parallelGateway = ParallelGateway.builder()
                .id(TEST_ID)
                .name(TEST_NAME)
                .build();

        // Then
        assertNotNull(parallelGateway);
        assertEquals(TEST_ID, parallelGateway.id());
        assertEquals(TEST_NAME, parallelGateway.name());
        assertEquals(ActivityType.PARALLEL_GATEWAY, parallelGateway.type());
        assertNull(parallelGateway.parentId());
        assertNull(parallelGateway.incoming());
        assertNull(parallelGateway.outgoing());
    }

    @Test
    @DisplayName("Should support toBuilder functionality")
    void shouldSupportToBuilderFunctionality() {
        // Given
        var originalGateway = ParallelGateway.builder()
                .id(TEST_ID)
                .name(TEST_NAME)
                .incoming(TEST_INCOMING)
                .build();

        // When
        var modifiedGateway = originalGateway.toBuilder()
                .parentId(TEST_PARENT_ID)
                .outgoing(TEST_OUTGOING)
                .build();

        // Then
        assertEquals(TEST_ID, modifiedGateway.id());
        assertEquals(TEST_NAME, modifiedGateway.name());
        assertEquals(TEST_INCOMING, modifiedGateway.incoming());
        assertEquals(TEST_PARENT_ID, modifiedGateway.parentId());
        assertEquals(TEST_OUTGOING, modifiedGateway.outgoing());
        // Original should remain unchanged
        assertNull(originalGateway.parentId());
        assertNull(originalGateway.outgoing());
    }

    @Test
    @DisplayName("Should override type to return PARALLEL_GATEWAY")
    void shouldOverrideTypeToReturnParallelGateway() {
        // Given
        var parallelGateway = ParallelGateway.builder()
                .id(TEST_ID)
                .type(ActivityType.EXCLUSIVE_GATEWAY) // Different type in constructor
                .build();

        // When & Then
        assertEquals(ActivityType.PARALLEL_GATEWAY, parallelGateway.type());
    }

    @Test
    @DisplayName("Should return empty map for inputs")
    void shouldReturnEmptyMapForInputs() {
        // Given
        var parallelGateway = ParallelGateway.builder()
                .id(TEST_ID)
                .build();

        // When
        var inputs = parallelGateway.inputs();

        // Then
        assertNotNull(inputs);
        assertTrue(inputs.isEmpty());
    }

    @Test
    @DisplayName("Should return empty map for outputs")
    void shouldReturnEmptyMapForOutputs() {
        // Given
        var parallelGateway = ParallelGateway.builder()
                .id(TEST_ID)
                .build();

        // When
        var outputs = parallelGateway.outputs();

        // Then
        assertNotNull(outputs);
        assertTrue(outputs.isEmpty());
    }

    @Test
    @DisplayName("Should implement equals correctly")
    void shouldImplementEqualsCorrectly() {
        // Given
        var parallelGateway1 = ParallelGateway.builder()
                .id(TEST_ID)
                .name(TEST_NAME)
                .incoming(TEST_INCOMING)
                .outgoing(TEST_OUTGOING)
                .build();

        var parallelGateway2 = ParallelGateway.builder()
                .id(TEST_ID)
                .name(TEST_NAME)
                .incoming(TEST_INCOMING)
                .outgoing(TEST_OUTGOING)
                .build();

        var parallelGateway3 = ParallelGateway.builder()
                .id("differentId")
                .name(TEST_NAME)
                .incoming(TEST_INCOMING)
                .outgoing(TEST_OUTGOING)
                .build();

        // When & Then
        assertEquals(parallelGateway1, parallelGateway2);
        assertNotEquals(parallelGateway1, parallelGateway3);
        assertNotEquals(null, parallelGateway1);
    }

    @Test
    @DisplayName("Should implement hashCode correctly")
    void shouldImplementHashCodeCorrectly() {
        // Given
        var parallelGateway1 = ParallelGateway.builder()
                .id(TEST_ID)
                .name(TEST_NAME)
                .incoming(TEST_INCOMING)
                .outgoing(TEST_OUTGOING)
                .build();

        var parallelGateway2 = ParallelGateway.builder()
                .id(TEST_ID)
                .name(TEST_NAME)
                .incoming(TEST_INCOMING)
                .outgoing(TEST_OUTGOING)
                .build();

        // When & Then
        assertEquals(parallelGateway1.hashCode(), parallelGateway2.hashCode());
    }

    @Test
    @DisplayName("Should implement toString correctly")
    void shouldImplementToStringCorrectly() {
        // Given
        var parallelGateway = ParallelGateway.builder()
                .id(TEST_ID)
                .name(TEST_NAME)
                .build();

        // When
        var toStringResult = parallelGateway.toString();

        // Then
        assertNotNull(toStringResult);
        assertTrue(toStringResult.contains("ParallelGateway"));
        assertTrue(toStringResult.contains(TEST_ID));
        assertTrue(toStringResult.contains(TEST_NAME));
    }

    @Test
    @DisplayName("Should handle multiple incoming and outgoing connections")
    void shouldHandleMultipleIncomingAndOutgoingConnections() {
        // Given - Parallel gateways typically have multiple connections
        var multipleIncoming = List.of("task1", "task2", "task3");
        var multipleOutgoing = List.of("task4", "task5", "task6", "task7");

        // When
        var parallelGateway = ParallelGateway.builder()
                .id(TEST_ID)
                .incoming(multipleIncoming)
                .outgoing(multipleOutgoing)
                .build();

        // Then
        assertNotNull(parallelGateway);
        assertEquals(multipleIncoming, parallelGateway.incoming());
        assertEquals(multipleOutgoing, parallelGateway.outgoing());
        assertEquals(3, parallelGateway.incoming().size());
        assertEquals(4, parallelGateway.outgoing().size());
    }

    @Test
    @DisplayName("Should work as ActivityDefinition interface")
    void shouldWorkAsActivityDefinitionInterface() {
        // Given
        var parallelGateway = ParallelGateway.builder()
                .id(TEST_ID)
                .parentId(TEST_PARENT_ID)
                .name(TEST_NAME)
                .incoming(TEST_INCOMING)
                .outgoing(TEST_OUTGOING)
                .build();

        // When - casting to interface
        var activityDefinition = (com.leorces.model.definition.activity.ActivityDefinition) parallelGateway;

        // Then
        assertEquals(TEST_ID, activityDefinition.id());
        assertEquals(TEST_PARENT_ID, activityDefinition.parentId());
        assertEquals(TEST_NAME, activityDefinition.name());
        assertEquals(ActivityType.PARALLEL_GATEWAY, activityDefinition.type());
        assertEquals(TEST_INCOMING, activityDefinition.incoming());
        assertEquals(TEST_OUTGOING, activityDefinition.outgoing());
        assertTrue(activityDefinition.inputs().isEmpty());
        assertTrue(activityDefinition.outputs().isEmpty());
    }

    @Test
    @DisplayName("Should handle empty lists for incoming and outgoing")
    void shouldHandleEmptyListsForIncomingAndOutgoing() {
        // Given
        var emptyIncoming = List.<String>of();
        var emptyOutgoing = List.<String>of();

        // When
        var parallelGateway = ParallelGateway.builder()
                .id(TEST_ID)
                .incoming(emptyIncoming)
                .outgoing(emptyOutgoing)
                .build();

        // Then
        assertNotNull(parallelGateway);
        assertEquals(emptyIncoming, parallelGateway.incoming());
        assertEquals(emptyOutgoing, parallelGateway.outgoing());
        assertTrue(parallelGateway.incoming().isEmpty());
        assertTrue(parallelGateway.outgoing().isEmpty());
    }
}