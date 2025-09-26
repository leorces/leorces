package com.leorces.model.definition.activity.event;

import com.leorces.model.definition.activity.ActivityType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("IntermediateCatchEvent Tests")
class IntermediateCatchEventTest {

    private static final String TEST_ID = "intermediate-catch-event-123";
    private static final String TEST_PARENT_ID = "parent-456";
    private static final String TEST_NAME = "Test Intermediate Catch Event";
    private static final String TEST_CONDITION = "${orderAmount > 1000}";
    private static final String TEST_VARIABLE_NAME = "orderData";
    private static final String TEST_VARIABLE_EVENTS = "orderReceived,orderValidated";
    private static final Map<String, Object> TEST_INPUTS = Map.of("orderId", "12345", "threshold", 1000);
    private static final Map<String, Object> TEST_OUTPUTS = Map.of("result", "caught", "eventType", "order");

    @Test
    @DisplayName("Should create IntermediateCatchEvent with builder pattern")
    void shouldCreateIntermediateCatchEventWithBuilder() {
        // Given
        var incoming = List.of("incoming1", "incoming2");
        var outgoing = List.of("outgoing1", "outgoing2");

        // When
        var intermediateCatchEvent = IntermediateCatchEvent.builder()
                .id(TEST_ID)
                .parentId(TEST_PARENT_ID)
                .name(TEST_NAME)
                .condition(TEST_CONDITION)
                .variableName(TEST_VARIABLE_NAME)
                .variableEvents(TEST_VARIABLE_EVENTS)
                .type(ActivityType.INTERMEDIATE_CATCH_EVENT)
                .incoming(incoming)
                .outgoing(outgoing)
                .inputs(TEST_INPUTS)
                .outputs(TEST_OUTPUTS)
                .build();

        // Then
        assertNotNull(intermediateCatchEvent);
        assertEquals(TEST_ID, intermediateCatchEvent.id());
        assertEquals(TEST_PARENT_ID, intermediateCatchEvent.parentId());
        assertEquals(TEST_NAME, intermediateCatchEvent.name());
        assertEquals(TEST_CONDITION, intermediateCatchEvent.condition());
        assertEquals(TEST_VARIABLE_NAME, intermediateCatchEvent.variableName());
        assertEquals(TEST_VARIABLE_EVENTS, intermediateCatchEvent.variableEvents());
        assertEquals(incoming, intermediateCatchEvent.incoming());
        assertEquals(outgoing, intermediateCatchEvent.outgoing());
        assertEquals(TEST_INPUTS, intermediateCatchEvent.inputs());
        assertEquals(TEST_OUTPUTS, intermediateCatchEvent.outputs());
    }

    @Test
    @DisplayName("Should create IntermediateCatchEvent with minimal fields")
    void shouldCreateIntermediateCatchEventWithMinimalFields() {
        // Given & When
        var intermediateCatchEvent = IntermediateCatchEvent.builder()
                .id(TEST_ID)
                .name(TEST_NAME)
                .condition(TEST_CONDITION)
                .build();

        // Then
        assertNotNull(intermediateCatchEvent);
        assertEquals(TEST_ID, intermediateCatchEvent.id());
        assertEquals(TEST_NAME, intermediateCatchEvent.name());
        assertEquals(TEST_CONDITION, intermediateCatchEvent.condition());
        assertNull(intermediateCatchEvent.parentId());
        assertNull(intermediateCatchEvent.variableName());
        assertNull(intermediateCatchEvent.variableEvents());
        assertNull(intermediateCatchEvent.incoming());
        assertNull(intermediateCatchEvent.outgoing());
        assertNull(intermediateCatchEvent.inputs());
        assertNull(intermediateCatchEvent.outputs());
    }

    @Test
    @DisplayName("Should always return INTERMEDIATE_CATCH_EVENT type")
    void shouldAlwaysReturnIntermediateCatchEventType() {
        // Given
        var intermediateCatchEvent = IntermediateCatchEvent.builder()
                .id(TEST_ID)
                .name(TEST_NAME)
                .condition(TEST_CONDITION)
                .type(ActivityType.START_EVENT) // Try to set different type
                .build();

        // When
        var type = intermediateCatchEvent.type();

        // Then
        assertEquals(ActivityType.INTERMEDIATE_CATCH_EVENT, type);
    }

    @Test
    @DisplayName("Should handle different conditions")
    void shouldHandleDifferentConditions() {
        // Given
        var simpleCondition = IntermediateCatchEvent.builder()
                .id("simple-event")
                .name("Simple Condition Event")
                .condition("${status == 'APPROVED'}")
                .build();

        var complexCondition = IntermediateCatchEvent.builder()
                .id("complex-event")
                .name("Complex Condition Event")
                .condition("${orderAmount > 1000 && customerType == 'PREMIUM'}")
                .build();

        // When & Then
        assertEquals("${status == 'APPROVED'}", simpleCondition.condition());
        assertEquals("${orderAmount > 1000 && customerType == 'PREMIUM'}", complexCondition.condition());
        assertNotEquals(simpleCondition.condition(), complexCondition.condition());
    }

    @Test
    @DisplayName("Should handle variable names and events")
    void shouldHandleVariableNamesAndEvents() {
        // Given
        var orderEvent = IntermediateCatchEvent.builder()
                .id("order-event")
                .name("Order Catch Event")
                .condition("${orderReceived}")
                .variableName("orderData")
                .variableEvents("orderCreated,orderValidated")
                .build();

        var paymentEvent = IntermediateCatchEvent.builder()
                .id("payment-event")
                .name("Payment Catch Event")
                .condition("${paymentReceived}")
                .variableName("paymentInfo")
                .variableEvents("paymentStarted,paymentCompleted,paymentFailed")
                .build();

        // When & Then
        assertEquals("orderData", orderEvent.variableName());
        assertEquals("orderCreated,orderValidated", orderEvent.variableEvents());
        assertEquals("paymentInfo", paymentEvent.variableName());
        assertEquals("paymentStarted,paymentCompleted,paymentFailed", paymentEvent.variableEvents());
    }

    @Test
    @DisplayName("Should handle actual inputs and outputs maps")
    void shouldHandleActualInputsAndOutputsMaps() {
        // Given
        var inputs = Map.<String, Object>of("threshold", 500, "category", "electronics", "active", true);
        var outputs = Map.of("eventCaught", true, "timestamp", "2024-01-01T10:00:00", "data", Map.<String, Object>of("nested", "value"));

        // When
        var intermediateCatchEvent = IntermediateCatchEvent.builder()
                .id(TEST_ID)
                .name(TEST_NAME)
                .condition(TEST_CONDITION)
                .inputs(inputs)
                .outputs(outputs)
                .build();

        // Then
        assertEquals(inputs, intermediateCatchEvent.inputs());
        assertEquals(outputs, intermediateCatchEvent.outputs());
        assertFalse(intermediateCatchEvent.inputs().isEmpty());
        assertFalse(intermediateCatchEvent.outputs().isEmpty());
        assertEquals(500, intermediateCatchEvent.inputs().get("threshold"));
        assertEquals(true, intermediateCatchEvent.outputs().get("eventCaught"));
    }

    @Test
    @DisplayName("Should handle empty and null collections")
    void shouldHandleEmptyAndNullCollections() {
        // Given
        var emptyIncoming = List.<String>of();
        var emptyOutgoing = List.<String>of();
        var emptyInputs = Map.<String, Object>of();
        var emptyOutputs = Map.<String, Object>of();

        // When
        var emptyEvent = IntermediateCatchEvent.builder()
                .id("empty-event")
                .name("Empty Event")
                .condition("${always}")
                .incoming(emptyIncoming)
                .outgoing(emptyOutgoing)
                .inputs(emptyInputs)
                .outputs(emptyOutputs)
                .build();

        var nullEvent = IntermediateCatchEvent.builder()
                .id("null-event")
                .name("Null Event")
                .condition("${never}")
                .incoming(null)
                .outgoing(null)
                .inputs(null)
                .outputs(null)
                .build();

        // Then
        assertTrue(emptyEvent.incoming().isEmpty());
        assertTrue(emptyEvent.outgoing().isEmpty());
        assertTrue(emptyEvent.inputs().isEmpty());
        assertTrue(emptyEvent.outputs().isEmpty());

        assertNull(nullEvent.incoming());
        assertNull(nullEvent.outgoing());
        assertNull(nullEvent.inputs());
        assertNull(nullEvent.outputs());
    }

    @Test
    @DisplayName("Should support toBuilder functionality")
    void shouldSupportToBuilderFunctionality() {
        // Given
        var originalEvent = IntermediateCatchEvent.builder()
                .id(TEST_ID)
                .parentId(TEST_PARENT_ID)
                .name(TEST_NAME)
                .condition(TEST_CONDITION)
                .variableName(TEST_VARIABLE_NAME)
                .inputs(TEST_INPUTS)
                .build();

        // When
        var modifiedEvent = originalEvent.toBuilder()
                .name("Modified Intermediate Catch Event")
                .condition("${modifiedCondition}")
                .variableName("modifiedVariable")
                .variableEvents("event1,event2")
                .outputs(TEST_OUTPUTS)
                .build();

        // Then
        assertNotEquals(originalEvent, modifiedEvent);
        assertEquals(TEST_ID, modifiedEvent.id());
        assertEquals(TEST_PARENT_ID, modifiedEvent.parentId());
        assertEquals("Modified Intermediate Catch Event", modifiedEvent.name());
        assertEquals("${modifiedCondition}", modifiedEvent.condition());
        assertEquals("modifiedVariable", modifiedEvent.variableName());
        assertEquals("event1,event2", modifiedEvent.variableEvents());
        assertEquals(TEST_INPUTS, modifiedEvent.inputs());
        assertEquals(TEST_OUTPUTS, modifiedEvent.outputs());
    }

    @Test
    @DisplayName("Should implement equals correctly")
    void shouldImplementEqualsCorrectly() {
        // Given
        var event1 = IntermediateCatchEvent.builder()
                .id(TEST_ID)
                .name(TEST_NAME)
                .condition(TEST_CONDITION)
                .variableName(TEST_VARIABLE_NAME)
                .inputs(TEST_INPUTS)
                .build();

        var event2 = IntermediateCatchEvent.builder()
                .id(TEST_ID)
                .name(TEST_NAME)
                .condition(TEST_CONDITION)
                .variableName(TEST_VARIABLE_NAME)
                .inputs(TEST_INPUTS)
                .build();

        var event3 = IntermediateCatchEvent.builder()
                .id("different-id")
                .name(TEST_NAME)
                .condition(TEST_CONDITION)
                .variableName(TEST_VARIABLE_NAME)
                .inputs(TEST_INPUTS)
                .build();

        var event4 = IntermediateCatchEvent.builder()
                .id(TEST_ID)
                .name(TEST_NAME)
                .condition("${differentCondition}")
                .variableName(TEST_VARIABLE_NAME)
                .inputs(TEST_INPUTS)
                .build();

        // When & Then
        assertEquals(event1, event2);
        assertNotEquals(event1, event3);
        assertNotEquals(event1, event4);
        assertNotEquals(null, event1);
    }

    @Test
    @DisplayName("Should implement hashCode correctly")
    void shouldImplementHashCodeCorrectly() {
        // Given
        var event1 = IntermediateCatchEvent.builder()
                .id(TEST_ID)
                .name(TEST_NAME)
                .condition(TEST_CONDITION)
                .variableName(TEST_VARIABLE_NAME)
                .inputs(TEST_INPUTS)
                .build();

        var event2 = IntermediateCatchEvent.builder()
                .id(TEST_ID)
                .name(TEST_NAME)
                .condition(TEST_CONDITION)
                .variableName(TEST_VARIABLE_NAME)
                .inputs(TEST_INPUTS)
                .build();

        // When & Then
        assertEquals(event1.hashCode(), event2.hashCode());
    }

    @Test
    @DisplayName("Should implement toString correctly")
    void shouldImplementToStringCorrectly() {
        // Given
        var intermediateCatchEvent = IntermediateCatchEvent.builder()
                .id(TEST_ID)
                .name(TEST_NAME)
                .parentId(TEST_PARENT_ID)
                .condition(TEST_CONDITION)
                .variableName(TEST_VARIABLE_NAME)
                .variableEvents(TEST_VARIABLE_EVENTS)
                .build();

        // When
        var toStringResult = intermediateCatchEvent.toString();

        // Then
        assertNotNull(toStringResult);
        assertTrue(toStringResult.contains("IntermediateCatchEvent"));
        assertTrue(toStringResult.contains(TEST_ID));
        assertTrue(toStringResult.contains(TEST_NAME));
        assertTrue(toStringResult.contains(TEST_PARENT_ID));
        assertTrue(toStringResult.contains(TEST_CONDITION));
        assertTrue(toStringResult.contains(TEST_VARIABLE_NAME));
        assertTrue(toStringResult.contains(TEST_VARIABLE_EVENTS));
    }

    @Test
    @DisplayName("Should work as ActivityDefinition implementation")
    void shouldWorkAsActivityDefinitionImplementation() {
        // Given
        var intermediateCatchEvent = IntermediateCatchEvent.builder()
                .id(TEST_ID)
                .name(TEST_NAME)
                .condition(TEST_CONDITION)
                .incoming(List.of("previous-task"))
                .outgoing(List.of("next-task"))
                .inputs(TEST_INPUTS)
                .outputs(TEST_OUTPUTS)
                .build();

        // When & Then - Test ActivityDefinition interface methods
        assertEquals(TEST_ID, intermediateCatchEvent.id());
        assertEquals(TEST_NAME, intermediateCatchEvent.name());
        assertEquals(ActivityType.INTERMEDIATE_CATCH_EVENT, intermediateCatchEvent.type());
        assertEquals(1, intermediateCatchEvent.incoming().size());
        assertEquals(1, intermediateCatchEvent.outgoing().size());
        assertEquals(TEST_INPUTS, intermediateCatchEvent.inputs());
        assertEquals(TEST_OUTPUTS, intermediateCatchEvent.outputs());
    }

    @Test
    @DisplayName("Should work as ConditionalActivityDefinition implementation")
    void shouldWorkAsConditionalActivityDefinitionImplementation() {
        // Given
        var intermediateCatchEvent = IntermediateCatchEvent.builder()
                .id(TEST_ID)
                .name(TEST_NAME)
                .condition(TEST_CONDITION)
                .build();

        // When & Then - Test ConditionalActivityDefinition interface methods
        assertEquals(TEST_CONDITION, intermediateCatchEvent.condition());

        // Also test inherited ActivityDefinition methods
        assertEquals(TEST_ID, intermediateCatchEvent.id());
        assertEquals(TEST_NAME, intermediateCatchEvent.name());
        assertEquals(ActivityType.INTERMEDIATE_CATCH_EVENT, intermediateCatchEvent.type());
    }

    @Test
    @DisplayName("Should handle subprocess context")
    void shouldHandleSubprocessContext() {
        // Given
        var subprocessCatchEvent = IntermediateCatchEvent.builder()
                .id("subprocess-catch-event")
                .parentId("subprocess-parent")
                .name("Subprocess Catch Event")
                .condition("${subprocessCondition}")
                .variableName("subprocessVar")
                .incoming(List.of("subprocess-task"))
                .outgoing(List.of("subprocess-next"))
                .build();

        // When & Then
        assertEquals("subprocess-catch-event", subprocessCatchEvent.id());
        assertEquals("subprocess-parent", subprocessCatchEvent.parentId());
        assertEquals("Subprocess Catch Event", subprocessCatchEvent.name());
        assertEquals("${subprocessCondition}", subprocessCatchEvent.condition());
        assertEquals("subprocessVar", subprocessCatchEvent.variableName());
        assertEquals(ActivityType.INTERMEDIATE_CATCH_EVENT, subprocessCatchEvent.type());
    }

    @Test
    @DisplayName("Should handle null condition and variable fields")
    void shouldHandleNullConditionAndVariableFields() {
        // Given & When
        var intermediateCatchEvent = IntermediateCatchEvent.builder()
                .id(TEST_ID)
                .name(TEST_NAME)
                .condition(null)
                .variableName(null)
                .variableEvents(null)
                .build();

        // Then
        assertNotNull(intermediateCatchEvent);
        assertEquals(TEST_ID, intermediateCatchEvent.id());
        assertEquals(TEST_NAME, intermediateCatchEvent.name());
        assertNull(intermediateCatchEvent.condition());
        assertNull(intermediateCatchEvent.variableName());
        assertNull(intermediateCatchEvent.variableEvents());
    }

    @Test
    @DisplayName("Should handle intermediate catch event scenarios")
    void shouldHandleIntermediateCatchEventScenarios() {
        // Given
        var timerCatchEvent = IntermediateCatchEvent.builder()
                .id("timer-catch")
                .name("Timer Catch Event")
                .condition("${waitDuration}")
                .variableName("timerData")
                .variableEvents("timerExpired")
                .incoming(List.of("start-wait"))
                .outgoing(List.of("continue-process"))
                .build();

        var messageCatchEvent = IntermediateCatchEvent.builder()
                .id("message-catch")
                .name("Message Catch Event")
                .condition("${messageReceived}")
                .variableName("messageContent")
                .variableEvents("messageArrived,messageProcessed")
                .incoming(List.of("wait-message"))
                .outgoing(List.of("handle-message"))
                .build();

        // When & Then
        assertEquals(ActivityType.INTERMEDIATE_CATCH_EVENT, timerCatchEvent.type());
        assertEquals(ActivityType.INTERMEDIATE_CATCH_EVENT, messageCatchEvent.type());
        assertEquals("${waitDuration}", timerCatchEvent.condition());
        assertEquals("${messageReceived}", messageCatchEvent.condition());
        assertEquals("timerData", timerCatchEvent.variableName());
        assertEquals("messageContent", messageCatchEvent.variableName());
        assertTrue(timerCatchEvent.outgoing().contains("continue-process"));
        assertTrue(messageCatchEvent.outgoing().contains("handle-message"));
    }

}