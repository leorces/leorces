package com.leorces.model.definition.activity.task;

import com.leorces.model.definition.activity.ActivityType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("External Task Tests")
class ExternalTaskTest {

    private static final String TEST_ID = "externalTask1";
    private static final String TEST_NAME = "External Task";
    private static final String TEST_TOPIC = "order-processing";
    private static final int TEST_RETRIES = 3;
    private static final Map<String, Object> TEST_INPUTS = Map.of("orderId", "12345", "amount", 1000);
    private static final Map<String, Object> TEST_OUTPUTS = Map.of("result", "processed", "status", "completed");

    @Test
    @DisplayName("Should create ExternalTask with all fields")
    void shouldCreateExternalTaskWithAllFields() {
        // When
        var externalTask = ExternalTask.builder()
                .id(TEST_ID)
                .name(TEST_NAME)
                .topic(TEST_TOPIC)
                .retries(TEST_RETRIES)
                .inputs(TEST_INPUTS)
                .outputs(TEST_OUTPUTS)
                .build();

        // Then
        assertNotNull(externalTask);
        assertEquals(TEST_ID, externalTask.id());
        assertEquals(TEST_NAME, externalTask.name());
        assertEquals(TEST_TOPIC, externalTask.topic());
        assertEquals(TEST_RETRIES, externalTask.retries());
        assertEquals(ActivityType.EXTERNAL_TASK, externalTask.type());
        assertEquals(TEST_INPUTS, externalTask.inputs());
        assertEquals(TEST_OUTPUTS, externalTask.outputs());
    }

    @Test
    @DisplayName("Should override type to return EXTERNAL_TASK")
    void shouldOverrideTypeToReturnExternalTask() {
        // When
        var externalTask = ExternalTask.builder()
                .id(TEST_ID)
                .type(ActivityType.RECEIVE_TASK)
                .build();

        // Then
        assertEquals(ActivityType.EXTERNAL_TASK, externalTask.type());
    }

    @Test
    @DisplayName("Should support toBuilder functionality")
    void shouldSupportToBuilderFunctionality() {
        // Given
        var originalTask = ExternalTask.builder()
                .id(TEST_ID)
                .topic(TEST_TOPIC)
                .retries(1)
                .build();

        // When
        var modifiedTask = originalTask.toBuilder()
                .retries(TEST_RETRIES)
                .inputs(TEST_INPUTS)
                .build();

        // Then
        assertEquals(TEST_ID, modifiedTask.id());
        assertEquals(TEST_TOPIC, modifiedTask.topic());
        assertEquals(TEST_RETRIES, modifiedTask.retries());
        assertEquals(TEST_INPUTS, modifiedTask.inputs());
    }

    @Test
    @DisplayName("Should handle different retry counts")
    void shouldHandleDifferentRetryCounts() {
        // Given
        var zeroRetries = ExternalTask.builder().id("task1").retries(0).build();
        var maxRetries = ExternalTask.builder().id("task2").retries(10).build();

        // When & Then
        assertEquals(0, zeroRetries.retries());
        assertEquals(10, maxRetries.retries());
    }

    @Test
    @DisplayName("Should handle different topics")
    void shouldHandleDifferentTopics() {
        // Given
        var paymentTask = ExternalTask.builder().id("payment").topic("payment-processing").build();
        var notificationTask = ExternalTask.builder().id("notify").topic("send-notification").build();

        // When & Then
        assertEquals("payment-processing", paymentTask.topic());
        assertEquals("send-notification", notificationTask.topic());
    }

    @Test
    @DisplayName("Should return actual inputs and outputs maps")
    void shouldReturnActualInputsAndOutputsMaps() {
        // Given
        Map<String, Object> inputs = Map.of("key1", "value1", "key2", 42);
        Map<String, Object> outputs = Map.of("result", true, "message", "success");

        // When
        var externalTask = ExternalTask.builder()
                .id(TEST_ID)
                .inputs(inputs)
                .outputs(outputs)
                .build();

        // Then
        assertEquals(inputs, externalTask.inputs());
        assertEquals(outputs, externalTask.outputs());
        assertFalse(externalTask.inputs().isEmpty());
        assertFalse(externalTask.outputs().isEmpty());
    }

    @Test
    @DisplayName("Should implement equals and hashCode correctly")
    void shouldImplementEqualsAndHashCodeCorrectly() {
        // Given
        var task1 = ExternalTask.builder()
                .id(TEST_ID)
                .topic(TEST_TOPIC)
                .retries(TEST_RETRIES)
                .build();

        var task2 = ExternalTask.builder()
                .id(TEST_ID)
                .topic(TEST_TOPIC)
                .retries(TEST_RETRIES)
                .build();

        // When & Then
        assertEquals(task1, task2);
        assertEquals(task1.hashCode(), task2.hashCode());
    }

    @Test
    @DisplayName("Should work as ActivityDefinition interface")
    void shouldWorkAsActivityDefinitionInterface() {
        // Given
        var externalTask = ExternalTask.builder()
                .id(TEST_ID)
                .name(TEST_NAME)
                .topic(TEST_TOPIC)
                .inputs(TEST_INPUTS)
                .outputs(TEST_OUTPUTS)
                .build();

        // When
        var activityDefinition = (com.leorces.model.definition.activity.ActivityDefinition) externalTask;

        // Then
        assertEquals(TEST_ID, activityDefinition.id());
        assertEquals(TEST_NAME, activityDefinition.name());
        assertEquals(ActivityType.EXTERNAL_TASK, activityDefinition.type());
        assertEquals(TEST_INPUTS, activityDefinition.inputs());
        assertEquals(TEST_OUTPUTS, activityDefinition.outputs());
    }

    @Test
    @DisplayName("Should handle null inputs and outputs")
    void shouldHandleNullInputsAndOutputs() {
        // When
        var externalTask = ExternalTask.builder()
                .id(TEST_ID)
                .inputs(null)
                .outputs(null)
                .build();

        // Then
        assertEquals(TEST_ID, externalTask.id());
        assertNull(externalTask.inputs());
        assertNull(externalTask.outputs());
    }

}