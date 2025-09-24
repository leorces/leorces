package com.leorces.model.definition.activity.gateway;

import com.leorces.model.definition.activity.ActivityType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Inclusive Gateway Tests")
class InclusiveGatewayTest {

    private static final String TEST_ID = "inclusiveGateway1";
    private static final String TEST_NAME = "Inclusive Gateway";
    private static final Map<String, List<String>> TEST_CONDITION = Map.of(
            "task2", List.of("${amount > 1000}", "${priority == 'HIGH'}"),
            "task3", List.of("${amount <= 1000}")
    );

    @Test
    @DisplayName("Should create InclusiveGateway with all fields")
    void shouldCreateInclusiveGatewayWithAllFields() {
        // When
        var inclusiveGateway = InclusiveGateway.builder()
                .id(TEST_ID)
                .name(TEST_NAME)
                .condition(TEST_CONDITION)
                .build();

        // Then
        assertNotNull(inclusiveGateway);
        assertEquals(TEST_ID, inclusiveGateway.id());
        assertEquals(TEST_NAME, inclusiveGateway.name());
        assertEquals(ActivityType.INCLUSIVE_GATEWAY, inclusiveGateway.type());
        assertEquals(TEST_CONDITION, inclusiveGateway.condition());
    }

    @Test
    @DisplayName("Should override type to return INCLUSIVE_GATEWAY")
    void shouldOverrideTypeToReturnInclusiveGateway() {
        // When
        var inclusiveGateway = InclusiveGateway.builder()
                .id(TEST_ID)
                .type(ActivityType.PARALLEL_GATEWAY)
                .build();

        // Then
        assertEquals(ActivityType.INCLUSIVE_GATEWAY, inclusiveGateway.type());
    }

    @Test
    @DisplayName("Should support toBuilder functionality")
    void shouldSupportToBuilderFunctionality() {
        // Given
        var originalGateway = InclusiveGateway.builder()
                .id(TEST_ID)
                .name(TEST_NAME)
                .build();

        // When
        var modifiedGateway = originalGateway.toBuilder()
                .condition(TEST_CONDITION)
                .build();

        // Then
        assertEquals(TEST_ID, modifiedGateway.id());
        assertEquals(TEST_CONDITION, modifiedGateway.condition());
    }

    @Test
    @DisplayName("Should return empty maps for inputs and outputs")
    void shouldReturnEmptyMapsForInputsAndOutputs() {
        // Given
        var inclusiveGateway = InclusiveGateway.builder().id(TEST_ID).build();

        // When & Then
        assertTrue(inclusiveGateway.inputs().isEmpty());
        assertTrue(inclusiveGateway.outputs().isEmpty());
    }

    @Test
    @DisplayName("Should handle multiple conditions per outgoing flow")
    void shouldHandleMultipleConditionsPerOutgoingFlow() {
        // Given
        Map<String, List<String>> multipleConditions = Map.of(
                "highPriority", List.of("${priority == 'HIGH'}", "${urgent == true}"),
                "lowPriority", List.of("${priority == 'LOW'}"),
                "defaultPath", List.of()
        );

        // When
        var inclusiveGateway = InclusiveGateway.builder()
                .id(TEST_ID)
                .condition(multipleConditions)
                .build();

        // Then
        assertEquals(multipleConditions, inclusiveGateway.condition());
        assertEquals(2, inclusiveGateway.condition().get("highPriority").size());
        assertEquals(1, inclusiveGateway.condition().get("lowPriority").size());
        assertTrue(inclusiveGateway.condition().get("defaultPath").isEmpty());
    }

    @Test
    @DisplayName("Should implement equals and hashCode correctly")
    void shouldImplementEqualsAndHashCodeCorrectly() {
        // Given
        var gateway1 = InclusiveGateway.builder()
                .id(TEST_ID)
                .condition(TEST_CONDITION)
                .build();

        var gateway2 = InclusiveGateway.builder()
                .id(TEST_ID)
                .condition(TEST_CONDITION)
                .build();

        // When & Then
        assertEquals(gateway1, gateway2);
        assertEquals(gateway1.hashCode(), gateway2.hashCode());
    }

    @Test
    @DisplayName("Should work as ActivityDefinition interface")
    void shouldWorkAsActivityDefinitionInterface() {
        // Given
        var inclusiveGateway = InclusiveGateway.builder()
                .id(TEST_ID)
                .name(TEST_NAME)
                .build();

        // When
        var activityDefinition = (com.leorces.model.definition.activity.ActivityDefinition) inclusiveGateway;

        // Then
        assertEquals(TEST_ID, activityDefinition.id());
        assertEquals(ActivityType.INCLUSIVE_GATEWAY, activityDefinition.type());
        assertTrue(activityDefinition.inputs().isEmpty());
        assertTrue(activityDefinition.outputs().isEmpty());
    }

    @Test
    @DisplayName("Should handle null condition")
    void shouldHandleNullCondition() {
        // When
        var inclusiveGateway = InclusiveGateway.builder()
                .id(TEST_ID)
                .condition(null)
                .build();

        // Then
        assertEquals(TEST_ID, inclusiveGateway.id());
        assertNull(inclusiveGateway.condition());
    }
}