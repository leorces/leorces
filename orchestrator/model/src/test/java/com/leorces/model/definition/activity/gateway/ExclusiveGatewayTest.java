package com.leorces.model.definition.activity.gateway;

import com.leorces.model.definition.activity.ActivityType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Exclusive Gateway Tests")
class ExclusiveGatewayTest {

    private static final String TEST_ID = "exclusiveGateway1";
    private static final String TEST_NAME = "Exclusive Gateway";
    private static final List<String> TEST_INCOMING = List.of("task1");
    private static final List<String> TEST_OUTGOING = List.of("task2", "task3");
    private static final Map<String, String> TEST_CONDITION = Map.of("task2", "${amount > 1000}", "task3", "${amount <= 1000}");

    @Test
    @DisplayName("Should create ExclusiveGateway with all fields")
    void shouldCreateExclusiveGatewayWithAllFields() {
        // When
        var exclusiveGateway = ExclusiveGateway.builder()
                .id(TEST_ID)
                .name(TEST_NAME)
                .incoming(TEST_INCOMING)
                .outgoing(TEST_OUTGOING)
                .condition(TEST_CONDITION)
                .build();

        // Then
        assertNotNull(exclusiveGateway);
        assertEquals(TEST_ID, exclusiveGateway.id());
        assertEquals(TEST_NAME, exclusiveGateway.name());
        assertEquals(ActivityType.EXCLUSIVE_GATEWAY, exclusiveGateway.type());
        assertEquals(TEST_INCOMING, exclusiveGateway.incoming());
        assertEquals(TEST_OUTGOING, exclusiveGateway.outgoing());
        assertEquals(TEST_CONDITION, exclusiveGateway.condition());
    }

    @Test
    @DisplayName("Should create ExclusiveGateway with null fields")
    void shouldCreateExclusiveGatewayWithNullFields() {
        // When
        var exclusiveGateway = ExclusiveGateway.builder()
                .id(TEST_ID)
                .condition(null)
                .build();

        // Then
        assertEquals(TEST_ID, exclusiveGateway.id());
        assertEquals(ActivityType.EXCLUSIVE_GATEWAY, exclusiveGateway.type());
        assertNull(exclusiveGateway.condition());
    }

    @Test
    @DisplayName("Should support toBuilder functionality")
    void shouldSupportToBuilderFunctionality() {
        // Given
        var originalGateway = ExclusiveGateway.builder()
                .id(TEST_ID)
                .name(TEST_NAME)
                .build();

        // When
        var modifiedGateway = originalGateway.toBuilder()
                .condition(TEST_CONDITION)
                .outgoing(TEST_OUTGOING)
                .build();

        // Then
        assertEquals(TEST_ID, modifiedGateway.id());
        assertEquals(TEST_CONDITION, modifiedGateway.condition());
        assertEquals(TEST_OUTGOING, modifiedGateway.outgoing());
    }

    @Test
    @DisplayName("Should override type to return EXCLUSIVE_GATEWAY")
    void shouldOverrideTypeToReturnExclusiveGateway() {
        // When
        var exclusiveGateway = ExclusiveGateway.builder()
                .id(TEST_ID)
                .type(ActivityType.PARALLEL_GATEWAY)
                .build();

        // Then
        assertEquals(ActivityType.EXCLUSIVE_GATEWAY, exclusiveGateway.type());
    }

    @Test
    @DisplayName("Should return empty maps for inputs and outputs")
    void shouldReturnEmptyMapsForInputsAndOutputs() {
        // Given
        var exclusiveGateway = ExclusiveGateway.builder().id(TEST_ID).build();

        // When & Then
        assertTrue(exclusiveGateway.inputs().isEmpty());
        assertTrue(exclusiveGateway.outputs().isEmpty());
    }

    @Test
    @DisplayName("Should handle different condition expressions")
    void shouldHandleDifferentConditionExpressions() {
        // Given
        var complexCondition = Map.of(
                "approve", "${amount > 10000 && priority == 'HIGH'}",
                "review", "${amount > 1000 && amount <= 10000}",
                "auto", "${amount <= 1000}"
        );

        // When
        var exclusiveGateway = ExclusiveGateway.builder()
                .id(TEST_ID)
                .condition(complexCondition)
                .build();

        // Then
        assertEquals(complexCondition, exclusiveGateway.condition());
        assertEquals(3, exclusiveGateway.condition().size());
    }

    @Test
    @DisplayName("Should implement equals and hashCode correctly")
    void shouldImplementEqualsAndHashCodeCorrectly() {
        // Given
        var gateway1 = ExclusiveGateway.builder()
                .id(TEST_ID)
                .condition(TEST_CONDITION)
                .build();

        var gateway2 = ExclusiveGateway.builder()
                .id(TEST_ID)
                .condition(TEST_CONDITION)
                .build();

        // When & Then
        assertEquals(gateway1, gateway2);
        assertEquals(gateway1.hashCode(), gateway2.hashCode());
    }

    @Test
    @DisplayName("Should implement toString correctly")
    void shouldImplementToStringCorrectly() {
        // Given
        var exclusiveGateway = ExclusiveGateway.builder()
                .id(TEST_ID)
                .name(TEST_NAME)
                .build();

        // When
        var toStringResult = exclusiveGateway.toString();

        // Then
        assertTrue(toStringResult.contains("ExclusiveGateway"));
        assertTrue(toStringResult.contains(TEST_ID));
        assertTrue(toStringResult.contains(TEST_NAME));
    }

    @Test
    @DisplayName("Should work as ActivityDefinition interface")
    void shouldWorkAsActivityDefinitionInterface() {
        // Given
        var exclusiveGateway = ExclusiveGateway.builder()
                .id(TEST_ID)
                .name(TEST_NAME)
                .build();

        // When
        var activityDefinition = (com.leorces.model.definition.activity.ActivityDefinition) exclusiveGateway;

        // Then
        assertEquals(TEST_ID, activityDefinition.id());
        assertEquals(ActivityType.EXCLUSIVE_GATEWAY, activityDefinition.type());
        assertTrue(activityDefinition.inputs().isEmpty());
        assertTrue(activityDefinition.outputs().isEmpty());
    }
}