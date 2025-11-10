package com.leorces.model.definition.activity.event.start;

import com.leorces.model.definition.activity.ActivityType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("MessageStartEvent Tests")
class MessageStartEventTest {

    private static final String TEST_ID = "message-start-event-123";
    private static final String TEST_PARENT_ID = "parent-456";
    private static final String TEST_NAME = "Test Message Start Event";
    private static final String TEST_MESSAGE_REFERENCE = "OrderSubmittedMessage";

    @Test
    @DisplayName("Should create MessageStartEvent with builder pattern")
    void shouldCreateMessageStartEventWithBuilder() {
        // Given
        var incoming = List.<String>of(); // Start events typically have no incoming
        var outgoing = List.of("outgoing1", "outgoing2");

        // When
        var messageStartEvent = MessageStartEvent.builder()
                .id(TEST_ID)
                .parentId(TEST_PARENT_ID)
                .name(TEST_NAME)
                .type(ActivityType.MESSAGE_START_EVENT)
                .incoming(incoming)
                .outgoing(outgoing)
                .messageReference(TEST_MESSAGE_REFERENCE)
                .build();

        // Then
        assertNotNull(messageStartEvent);
        assertEquals(TEST_ID, messageStartEvent.id());
        assertEquals(TEST_PARENT_ID, messageStartEvent.parentId());
        assertEquals(TEST_NAME, messageStartEvent.name());
        assertEquals(incoming, messageStartEvent.incoming());
        assertEquals(outgoing, messageStartEvent.outgoing());
        assertEquals(TEST_MESSAGE_REFERENCE, messageStartEvent.messageReference());
    }

    @Test
    @DisplayName("Should create MessageStartEvent with minimal fields")
    void shouldCreateMessageStartEventWithMinimalFields() {
        // Given & When
        var messageStartEvent = MessageStartEvent.builder()
                .id(TEST_ID)
                .name(TEST_NAME)
                .messageReference(TEST_MESSAGE_REFERENCE)
                .build();

        // Then
        assertNotNull(messageStartEvent);
        assertEquals(TEST_ID, messageStartEvent.id());
        assertEquals(TEST_NAME, messageStartEvent.name());
        assertEquals(TEST_MESSAGE_REFERENCE, messageStartEvent.messageReference());
        assertNull(messageStartEvent.parentId());
        assertNull(messageStartEvent.incoming());
        assertNull(messageStartEvent.outgoing());
    }

    @Test
    @DisplayName("Should always return MESSAGE_START_EVENT type")
    void shouldAlwaysReturnMessageStartEventType() {
        // Given
        var messageStartEvent = MessageStartEvent.builder()
                .id(TEST_ID)
                .name(TEST_NAME)
                .messageReference(TEST_MESSAGE_REFERENCE)
                .type(ActivityType.START_EVENT) // Try to set different type
                .build();

        // When
        var type = messageStartEvent.type();

        // Then
        assertEquals(ActivityType.MESSAGE_START_EVENT, type);
    }

    @Test
    @DisplayName("Should return empty inputs map")
    void shouldReturnEmptyInputsMap() {
        // Given
        var messageStartEvent = MessageStartEvent.builder()
                .id(TEST_ID)
                .name(TEST_NAME)
                .messageReference(TEST_MESSAGE_REFERENCE)
                .build();

        // When
        var inputs = messageStartEvent.inputs();

        // Then
        assertNotNull(inputs);
        assertTrue(inputs.isEmpty());
    }

    @Test
    @DisplayName("Should return empty outputs map")
    void shouldReturnEmptyOutputsMap() {
        // Given
        var messageStartEvent = MessageStartEvent.builder()
                .id(TEST_ID)
                .name(TEST_NAME)
                .messageReference(TEST_MESSAGE_REFERENCE)
                .build();

        // When
        var outputs = messageStartEvent.outputs();

        // Then
        assertNotNull(outputs);
        assertTrue(outputs.isEmpty());
    }

    @Test
    @DisplayName("Should handle different message references")
    void shouldHandleDifferentMessageReferences() {
        // Given
        var orderMessage = MessageStartEvent.builder()
                .id("order-start")
                .name("Order Message Start")
                .messageReference("OrderMessage")
                .build();

        var paymentMessage = MessageStartEvent.builder()
                .id("payment-start")
                .name("Payment Message Start")
                .messageReference("PaymentReceivedMessage")
                .build();

        // When & Then
        assertEquals("OrderMessage", orderMessage.messageReference());
        assertEquals("PaymentReceivedMessage", paymentMessage.messageReference());
        assertNotEquals(orderMessage.messageReference(), paymentMessage.messageReference());
    }

    @Test
    @DisplayName("Should handle multiple outgoing connections")
    void shouldHandleMultipleOutgoingConnections() {
        // Given
        var outgoing = List.of("task1", "task2", "gateway1", "subprocess1");

        // When
        var messageStartEvent = MessageStartEvent.builder()
                .id(TEST_ID)
                .name(TEST_NAME)
                .messageReference(TEST_MESSAGE_REFERENCE)
                .outgoing(outgoing)
                .build();

        // Then
        assertNotNull(messageStartEvent);
        assertEquals(4, messageStartEvent.outgoing().size());
        assertEquals("task1", messageStartEvent.outgoing().get(0));
        assertEquals("subprocess1", messageStartEvent.outgoing().get(3));
    }

    @Test
    @DisplayName("Should handle empty collections")
    void shouldHandleEmptyCollections() {
        // Given
        var emptyIncoming = List.<String>of();
        var emptyOutgoing = List.<String>of();

        // When
        var messageStartEvent = MessageStartEvent.builder()
                .id(TEST_ID)
                .name(TEST_NAME)
                .messageReference(TEST_MESSAGE_REFERENCE)
                .incoming(emptyIncoming)
                .outgoing(emptyOutgoing)
                .build();

        // Then
        assertNotNull(messageStartEvent);
        assertEquals(emptyIncoming, messageStartEvent.incoming());
        assertEquals(emptyOutgoing, messageStartEvent.outgoing());
        assertTrue(messageStartEvent.incoming().isEmpty());
        assertTrue(messageStartEvent.outgoing().isEmpty());
    }

    @Test
    @DisplayName("Should support toBuilder functionality")
    void shouldSupportToBuilderFunctionality() {
        // Given
        var originalEvent = MessageStartEvent.builder()
                .id(TEST_ID)
                .parentId(TEST_PARENT_ID)
                .name(TEST_NAME)
                .messageReference(TEST_MESSAGE_REFERENCE)
                .outgoing(List.of("original"))
                .build();

        // When
        var modifiedEvent = originalEvent.toBuilder()
                .name("Modified Message Start Event")
                .messageReference("ModifiedMessage")
                .outgoing(List.of("modified1", "modified2"))
                .build();

        // Then
        assertNotEquals(originalEvent, modifiedEvent);
        assertEquals(TEST_ID, modifiedEvent.id());
        assertEquals(TEST_PARENT_ID, modifiedEvent.parentId());
        assertEquals("Modified Message Start Event", modifiedEvent.name());
        assertEquals("ModifiedMessage", modifiedEvent.messageReference());
        assertEquals(List.of("modified1", "modified2"), modifiedEvent.outgoing());
    }

    @Test
    @DisplayName("Should implement equals correctly")
    void shouldImplementEqualsCorrectly() {
        // Given
        var event1 = MessageStartEvent.builder()
                .id(TEST_ID)
                .name(TEST_NAME)
                .messageReference(TEST_MESSAGE_REFERENCE)
                .outgoing(List.of("outgoing1"))
                .build();

        var event2 = MessageStartEvent.builder()
                .id(TEST_ID)
                .name(TEST_NAME)
                .messageReference(TEST_MESSAGE_REFERENCE)
                .outgoing(List.of("outgoing1"))
                .build();

        var event3 = MessageStartEvent.builder()
                .id("different-id")
                .name(TEST_NAME)
                .messageReference(TEST_MESSAGE_REFERENCE)
                .outgoing(List.of("outgoing1"))
                .build();

        var event4 = MessageStartEvent.builder()
                .id(TEST_ID)
                .name(TEST_NAME)
                .messageReference("DifferentMessage")
                .outgoing(List.of("outgoing1"))
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
        var event1 = MessageStartEvent.builder()
                .id(TEST_ID)
                .name(TEST_NAME)
                .messageReference(TEST_MESSAGE_REFERENCE)
                .outgoing(List.of("outgoing1"))
                .build();

        var event2 = MessageStartEvent.builder()
                .id(TEST_ID)
                .name(TEST_NAME)
                .messageReference(TEST_MESSAGE_REFERENCE)
                .outgoing(List.of("outgoing1"))
                .build();

        // When & Then
        assertEquals(event1.hashCode(), event2.hashCode());
    }

    @Test
    @DisplayName("Should implement toString correctly")
    void shouldImplementToStringCorrectly() {
        // Given
        var messageStartEvent = MessageStartEvent.builder()
                .id(TEST_ID)
                .name(TEST_NAME)
                .parentId(TEST_PARENT_ID)
                .messageReference(TEST_MESSAGE_REFERENCE)
                .build();

        // When
        var toStringResult = messageStartEvent.toString();

        // Then
        assertNotNull(toStringResult);
        assertTrue(toStringResult.contains("MessageStartEvent"));
        assertTrue(toStringResult.contains(TEST_ID));
        assertTrue(toStringResult.contains(TEST_NAME));
        assertTrue(toStringResult.contains(TEST_PARENT_ID));
        assertTrue(toStringResult.contains(TEST_MESSAGE_REFERENCE));
    }

    @Test
    @DisplayName("Should work as ActivityDefinition implementation")
    void shouldWorkAsActivityDefinitionImplementation() {
        // Given
        var messageStartEvent = MessageStartEvent.builder()
                .id(TEST_ID)
                .name(TEST_NAME)
                .messageReference(TEST_MESSAGE_REFERENCE)
                .incoming(List.of())
                .outgoing(List.of("next-task"))
                .build();

        // When & Then - Test ActivityDefinition interface methods
        assertEquals(TEST_ID, messageStartEvent.id());
        assertEquals(TEST_NAME, messageStartEvent.name());
        assertEquals(ActivityType.MESSAGE_START_EVENT, messageStartEvent.type());
        assertTrue(messageStartEvent.incoming().isEmpty());
        assertEquals(1, messageStartEvent.outgoing().size());
        assertTrue(messageStartEvent.inputs().isEmpty());
        assertTrue(messageStartEvent.outputs().isEmpty());
    }

    @Test
    @DisplayName("Should work as MessageActivityDefinition implementation")
    void shouldWorkAsMessageActivityDefinitionImplementation() {
        // Given
        var messageStartEvent = MessageStartEvent.builder()
                .id(TEST_ID)
                .name(TEST_NAME)
                .messageReference(TEST_MESSAGE_REFERENCE)
                .build();

        // When & Then - Test MessageActivityDefinition interface methods
        assertEquals(TEST_MESSAGE_REFERENCE, messageStartEvent.messageReference());

        // Also test inherited ActivityDefinition methods
        assertEquals(TEST_ID, messageStartEvent.id());
        assertEquals(TEST_NAME, messageStartEvent.name());
        assertEquals(ActivityType.MESSAGE_START_EVENT, messageStartEvent.type());
        assertTrue(messageStartEvent.inputs().isEmpty());
        assertTrue(messageStartEvent.outputs().isEmpty());
    }

    @Test
    @DisplayName("Should handle subprocess context")
    void shouldHandleSubprocessContext() {
        // Given
        var subprocessMessageStart = MessageStartEvent.builder()
                .id("subprocess-message-start")
                .parentId("subprocess-parent")
                .name("Subprocess Message Start")
                .messageReference("SubprocessMessage")
                .outgoing(List.of("subprocess-task"))
                .build();

        // When & Then
        assertEquals("subprocess-message-start", subprocessMessageStart.id());
        assertEquals("subprocess-parent", subprocessMessageStart.parentId());
        assertEquals("Subprocess Message Start", subprocessMessageStart.name());
        assertEquals("SubprocessMessage", subprocessMessageStart.messageReference());
        assertEquals(ActivityType.MESSAGE_START_EVENT, subprocessMessageStart.type());
        assertEquals(List.of("subprocess-task"), subprocessMessageStart.outgoing());
    }

    @Test
    @DisplayName("Should handle null messageReference")
    void shouldHandleNullMessageReference() {
        // Given & When
        var messageStartEvent = MessageStartEvent.builder()
                .id(TEST_ID)
                .name(TEST_NAME)
                .messageReference(null)
                .build();

        // Then
        assertNotNull(messageStartEvent);
        assertEquals(TEST_ID, messageStartEvent.id());
        assertEquals(TEST_NAME, messageStartEvent.name());
        assertNull(messageStartEvent.messageReference());
    }

    @Test
    @DisplayName("Should handle message-driven process scenarios")
    void shouldHandleMessageDrivenProcessScenarios() {
        // Given
        var orderStartEvent = MessageStartEvent.builder()
                .id("order-start")
                .name("Order Received Start")
                .messageReference("OrderSubmittedMessage")
                .outgoing(List.of("validate-order"))
                .build();

        var paymentStartEvent = MessageStartEvent.builder()
                .id("payment-start")
                .name("Payment Notification Start")
                .messageReference("PaymentCompletedMessage")
                .outgoing(List.of("process-payment"))
                .build();

        // When & Then
        assertEquals(ActivityType.MESSAGE_START_EVENT, orderStartEvent.type());
        assertEquals(ActivityType.MESSAGE_START_EVENT, paymentStartEvent.type());
        assertEquals("OrderSubmittedMessage", orderStartEvent.messageReference());
        assertEquals("PaymentCompletedMessage", paymentStartEvent.messageReference());
        assertTrue(orderStartEvent.outgoing().contains("validate-order"));
        assertTrue(paymentStartEvent.outgoing().contains("process-payment"));
    }

}