package com.leorces.extension.camunda;


import com.leorces.model.definition.activity.ActivityType;
import com.leorces.model.definition.activity.ConditionalActivityDefinition;
import com.leorces.model.definition.activity.MessageActivityDefinition;
import com.leorces.model.definition.activity.subprocess.CallActivity;
import com.leorces.model.definition.activity.task.ExternalTask;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;

import static org.assertj.core.api.Assertions.assertThat;


@DisplayName("BpmnParser Integration Tests")
@SpringBootTest(classes = BpmnParserTestConfiguration.class)
class BpmnParserTest {

    private static final String BPMN_FILES_PATH = "bpmn/";
    private static final String CAMUNDA_ORIGIN = "Camunda";

    @Autowired
    private BpmnParser bpmnParser;

    @Test
    @DisplayName("Should parse OrderFulfillmentProcess.bpmn")
    void shouldParseOrderFulfillmentProcess() {
        // Given
        var bpmnResource = new ClassPathResource(BPMN_FILES_PATH + "OrderFulfillmentProcess.bpmn");

        // When
        var processDefinition = bpmnParser.parse(bpmnResource);

        // Then - Process Definition basic fields
        assertThat(processDefinition.key()).isEqualTo("OrderFulfillmentProcess");
        assertThat(processDefinition.name()).isEqualTo("Order Fulfillment Process");

        // Then - Metadata verification
        assertThat(processDefinition.metadata()).isNotNull();
        assertThat(processDefinition.metadata().origin()).isEqualTo(CAMUNDA_ORIGIN);
        assertThat(processDefinition.metadata().deployment()).isEqualTo("OrderFulfillmentProcess.bpmn");
        assertThat(processDefinition.metadata().schema()).isNotNull();
        assertThat(processDefinition.metadata().schema()).contains("OrderFulfillmentProcess");

        // Then - Messages verification
        assertThat(processDefinition.messages()).hasSize(1);
        assertThat(processDefinition.messages()).contains("OrderFulfillmentFinishedMessage");

        // Then - Errors verification (should be empty for this BPMN)
        assertThat(processDefinition.errors()).isEmpty();

        // Then - Activities verification
        assertThat(processDefinition.activities()).isNotEmpty();

        // Verify start event
        var startEvent = processDefinition.activities().stream()
                .filter(activity -> "OrderFulfillmentProcessStartEvent".equals(activity.id()))
                .findFirst();
        assertThat(startEvent).isPresent();
        assertThat(startEvent.get().type()).isEqualTo(ActivityType.START_EVENT);
        assertThat(startEvent.get().name()).isEmpty(); // Start event has no name in BPMN
        assertThat(startEvent.get().parentId()).isNull();
        assertThat(startEvent.get().incoming()).isEmpty(); // Start event has no incoming flows
        assertThat(startEvent.get().outgoing()).containsExactly("OrderFulfillmentNotification");
        assertThat(startEvent.get().inputs()).isEmpty(); // Start event has no inputs
        assertThat(startEvent.get().outputs()).isEmpty(); // Start event has no outputs

        // Verify end event
        var endEvent = processDefinition.activities().stream()
                .filter(activity -> "OrderFulfillmentProcessEndEvent".equals(activity.id()))
                .findFirst();
        assertThat(endEvent).isPresent();
        assertThat(endEvent.get().type()).isEqualTo(ActivityType.END_EVENT);
        assertThat(endEvent.get().name()).isEmpty(); // End event has no name in BPMN
        assertThat(endEvent.get().parentId()).isNull();
        assertThat(endEvent.get().incoming()).containsExactly("OrderFulfillmentNotificationFinishedNotification");
        assertThat(endEvent.get().outgoing()).isEmpty(); // End event has no outgoing flows
        assertThat(endEvent.get().inputs()).isEmpty(); // End event has no inputs
        assertThat(endEvent.get().outputs()).isEmpty(); // End event has no outputs

        // Verify intermediate catch event
        var intermediateCatchEvent = processDefinition.activities().stream()
                .filter(activity -> "WaitOrderFulfillment".equals(activity.id()))
                .findFirst();
        assertThat(intermediateCatchEvent).isPresent();
        assertThat(intermediateCatchEvent.get().type()).isEqualTo(ActivityType.INTERMEDIATE_CATCH_EVENT);
        assertThat(intermediateCatchEvent.get().parentId()).isNull();
        assertThat(intermediateCatchEvent.get().incoming()).contains("OrderFulfillmentNotification");
        assertThat(intermediateCatchEvent.get().outgoing()).contains("OrderFulfillmentNotificationFinishedNotification");
        // Verify conditional expression if it's a conditional activity
        if (intermediateCatchEvent.get() instanceof ConditionalActivityDefinition conditionalActivity) {
            assertThat(conditionalActivity.condition()).isEqualTo("${orderFulfilled==true}");
        }

        // Verify subprocess
        var subprocess = processDefinition.activities().stream()
                .filter(activity -> "OrderFulfillment".equals(activity.id()))
                .findFirst();
        assertThat(subprocess).isPresent();
        assertThat(subprocess.get().type()).isEqualTo(ActivityType.EVENT_SUBPROCESS);
        assertThat(subprocess.get().name()).isEqualTo("Order fulfillment");
        assertThat(subprocess.get().parentId()).isNull();

        // Verify OrderFulfillment subprocess start event
        var subprocessStartEvent = processDefinition.activities().stream()
                .filter(activity -> "OrderFulfillmentStartEvent".equals(activity.id()))
                .findFirst();
        assertThat(subprocessStartEvent).isPresent();
        assertThat(subprocessStartEvent.get().type()).isEqualTo(ActivityType.MESSAGE_START_EVENT);
        assertThat(subprocessStartEvent.get().name()).isEmpty(); // Message start event has no name in BPMN
        assertThat(subprocessStartEvent.get().parentId()).isEqualTo("OrderFulfillment");
        assertThat(subprocessStartEvent.get().incoming()).isEmpty(); // Start event has no incoming flows
        assertThat(subprocessStartEvent.get().outgoing()).containsExactly("ProcessOrderFulfillment");
        assertThat(subprocessStartEvent.get().inputs()).isEmpty(); // Start event has no inputs
        assertThat(subprocessStartEvent.get().outputs()).isEmpty(); // Start event has no outputs
        // Verify message reference if it's a message activity
        if (subprocessStartEvent.get() instanceof MessageActivityDefinition messageActivity) {
            assertThat(messageActivity.messageReference()).isEqualTo("OrderFulfillmentFinishedMessage");
        }

        // Verify ProcessOrderFulfillment service task
        var processOrderFulfillmentTask = processDefinition.activities().stream()
                .filter(activity -> "ProcessOrderFulfillment".equals(activity.id()))
                .findFirst();
        assertThat(processOrderFulfillmentTask).isPresent();
        assertThat(processOrderFulfillmentTask.get().type()).isEqualTo(ActivityType.EXTERNAL_TASK);
        assertThat(processOrderFulfillmentTask.get().name()).isEqualTo("Process order fulfillment");
        assertThat(processOrderFulfillmentTask.get().parentId()).isEqualTo("OrderFulfillment");
        assertThat(processOrderFulfillmentTask.get().incoming()).containsExactly("OrderFulfillmentStartEvent");
        assertThat(processOrderFulfillmentTask.get().outgoing()).containsExactly("OrderFulfillmentEndEvent");
        assertThat(processOrderFulfillmentTask.get().inputs()).isEmpty(); // No inputs defined in BPMN
        assertThat(processOrderFulfillmentTask.get().outputs()).containsEntry("orderFulfilled", "true");
        // Verify external task-specific properties if it's an external task
        if (processOrderFulfillmentTask.get() instanceof ExternalTask externalTask) {
            assertThat(externalTask.topic()).isEqualTo("process-order-fulfillment");
            assertThat(externalTask.retries()).isGreaterThanOrEqualTo(0); // Default retries should be non-negative
        }

        // Verify OrderFulfillment subprocess end event
        var subprocessEndEvent = processDefinition.activities().stream()
                .filter(activity -> "OrderFulfillmentEndEvent".equals(activity.id()))
                .findFirst();
        assertThat(subprocessEndEvent).isPresent();
        assertThat(subprocessEndEvent.get().type()).isEqualTo(ActivityType.END_EVENT);
        assertThat(subprocessEndEvent.get().name()).isEmpty(); // End event has no name in BPMN
        assertThat(subprocessEndEvent.get().parentId()).isEqualTo("OrderFulfillment");
        assertThat(subprocessEndEvent.get().incoming()).containsExactly("ProcessOrderFulfillment");
        assertThat(subprocessEndEvent.get().outgoing()).isEmpty(); // End event has no outgoing flows
        assertThat(subprocessEndEvent.get().inputs()).isEmpty(); // End event has no inputs
        assertThat(subprocessEndEvent.get().outputs()).isEmpty(); // End event has no outputs

        // Verify send tasks
        var orderNotificationTask = processDefinition.activities().stream()
                .filter(activity -> "OrderFulfillmentNotification".equals(activity.id()))
                .findFirst();
        assertThat(orderNotificationTask).isPresent();
        assertThat(orderNotificationTask.get().type()).isEqualTo(ActivityType.EXTERNAL_TASK);
        assertThat(orderNotificationTask.get().name()).isEqualTo("Order fulfillment notification");
        assertThat(orderNotificationTask.get().parentId()).isNull();
        assertThat(orderNotificationTask.get().incoming()).containsExactly("OrderFulfillmentProcessStartEvent");
        assertThat(orderNotificationTask.get().outgoing()).containsExactly("WaitOrderFulfillment");
        assertThat(orderNotificationTask.get().inputs()).containsEntry("message", "Hi, ${client.firstName} ${client.lastName}! Your order ${order.number} will be fulfilled soon!");
        assertThat(orderNotificationTask.get().outputs()).isEmpty(); // No outputs defined in BPMN
        // Verify external task specific properties
        if (orderNotificationTask.get() instanceof ExternalTask externalTask) {
            assertThat(externalTask.topic()).isEqualTo("notification");
            assertThat(externalTask.retries()).isGreaterThanOrEqualTo(0);
        }

        var finishedNotificationTask = processDefinition.activities().stream()
                .filter(activity -> "OrderFulfillmentNotificationFinishedNotification".equals(activity.id()))
                .findFirst();
        assertThat(finishedNotificationTask).isPresent();
        assertThat(finishedNotificationTask.get().type()).isEqualTo(ActivityType.EXTERNAL_TASK);
        assertThat(finishedNotificationTask.get().name()).isEqualTo("Order fulfillment finished notification");
        assertThat(finishedNotificationTask.get().parentId()).isNull();
        assertThat(finishedNotificationTask.get().incoming()).containsExactly("WaitOrderFulfillment");
        assertThat(finishedNotificationTask.get().outgoing()).containsExactly("OrderFulfillmentProcessEndEvent");
        assertThat(finishedNotificationTask.get().inputs()).containsEntry("message", "Hi ${client.firstName} ${client.lastName}! Your order ${order.number} fullfiled! Will send it soon!");
        assertThat(finishedNotificationTask.get().outputs()).isEmpty(); // No outputs defined in BPMN
        // Verify external task specific properties
        if (finishedNotificationTask.get() instanceof ExternalTask externalTask) {
            assertThat(externalTask.topic()).isEqualTo("notification");
            assertThat(externalTask.retries()).isGreaterThanOrEqualTo(0);
        }

        // Verify total number of activities matches expected count
        assertThat(processDefinition.activities()).hasSize(9);
    }

    @Test
    @DisplayName("Should parse OrderDeliveryProcess.bpmn")
    void shouldParseOrderDeliveryProcess() {
        // Given
        var bpmnResource = new ClassPathResource(BPMN_FILES_PATH + "OrderDeliveryProcess.bpmn");

        // When
        var processDefinition = bpmnParser.parse(bpmnResource);

        // Then - Process Definition basic fields
        assertThat(processDefinition.key()).isEqualTo("OrderDeliveryProcess");
        assertThat(processDefinition.name()).isEqualTo("Order Delivery Process");

        // Then - Metadata verification
        assertThat(processDefinition.metadata()).isNotNull();
        assertThat(processDefinition.metadata().origin()).isEqualTo(CAMUNDA_ORIGIN);
        assertThat(processDefinition.metadata().deployment()).isEqualTo("OrderDeliveryProcess.bpmn");
        assertThat(processDefinition.metadata().schema()).isNotNull();
        assertThat(processDefinition.metadata().schema()).contains("OrderDeliveryProcess");

        // Then - Messages verification
        assertThat(processDefinition.messages()).hasSize(1);
        assertThat(processDefinition.messages()).contains("OrderDelivered");

        // Then - Errors verification (should be empty for this BPMN)
        assertThat(processDefinition.errors()).isEmpty();

        // Then - Activities verification
        assertThat(processDefinition.activities()).isNotEmpty();

        // Verify start event
        var startEvent = processDefinition.activities().stream()
                .filter(activity -> "OrderDeliveryProcessStartEvent".equals(activity.id()))
                .findFirst();
        assertThat(startEvent).isPresent();
        assertThat(startEvent.get().type()).isEqualTo(ActivityType.START_EVENT);
        assertThat(startEvent.get().name()).isEmpty();
        assertThat(startEvent.get().parentId()).isNull();
        assertThat(startEvent.get().incoming()).isEmpty();
        assertThat(startEvent.get().outgoing()).containsExactly("Gateway_1ob1wue");
        assertThat(startEvent.get().inputs()).isEmpty();
        assertThat(startEvent.get().outputs()).isEmpty();

        // Verify exclusive gateway (split)
        var exclusiveGateway = processDefinition.activities().stream()
                .filter(activity -> "Gateway_1ob1wue".equals(activity.id()))
                .findFirst();
        assertThat(exclusiveGateway).isPresent();
        assertThat(exclusiveGateway.get().type()).isEqualTo(ActivityType.EXCLUSIVE_GATEWAY);
        assertThat(exclusiveGateway.get().name()).isEmpty();
        assertThat(exclusiveGateway.get().parentId()).isNull();
        assertThat(exclusiveGateway.get().incoming()).containsExactly("OrderDeliveryProcessStartEvent");
        assertThat(exclusiveGateway.get().outgoing()).containsExactlyInAnyOrder("CourierDelivery", "ExpressDelivery", "PostalDelivery");
        assertThat(exclusiveGateway.get().inputs()).isEmpty();
        assertThat(exclusiveGateway.get().outputs()).isEmpty();

        // Verify courier delivery service task
        var courierDeliveryTask = processDefinition.activities().stream()
                .filter(activity -> "CourierDelivery".equals(activity.id()))
                .findFirst();
        assertThat(courierDeliveryTask).isPresent();
        assertThat(courierDeliveryTask.get().type()).isEqualTo(ActivityType.EXTERNAL_TASK);
        assertThat(courierDeliveryTask.get().name()).isEqualTo("Courier delivery");
        assertThat(courierDeliveryTask.get().parentId()).isNull();
        assertThat(courierDeliveryTask.get().incoming()).containsExactly("Gateway_1ob1wue");
        assertThat(courierDeliveryTask.get().outgoing()).containsExactly("Gateway_1kgd0tt");
        assertThat(courierDeliveryTask.get().inputs()).isEmpty();
        assertThat(courierDeliveryTask.get().outputs()).isEmpty();
        if (courierDeliveryTask.get() instanceof ExternalTask externalTask) {
            assertThat(externalTask.topic()).isEqualTo("courier-delivery");
            assertThat(externalTask.retries()).isGreaterThanOrEqualTo(0);
        }

        // Verify express delivery service task
        var expressDeliveryTask = processDefinition.activities().stream()
                .filter(activity -> "ExpressDelivery".equals(activity.id()))
                .findFirst();
        assertThat(expressDeliveryTask).isPresent();
        assertThat(expressDeliveryTask.get().type()).isEqualTo(ActivityType.EXTERNAL_TASK);
        assertThat(expressDeliveryTask.get().name()).isEqualTo("Express delivery");
        assertThat(expressDeliveryTask.get().parentId()).isNull();
        assertThat(expressDeliveryTask.get().incoming()).containsExactly("Gateway_1ob1wue");
        assertThat(expressDeliveryTask.get().outgoing()).containsExactly("Gateway_1kgd0tt");
        assertThat(expressDeliveryTask.get().inputs()).isEmpty();
        assertThat(expressDeliveryTask.get().outputs()).isEmpty();
        if (expressDeliveryTask.get() instanceof ExternalTask externalTask) {
            assertThat(externalTask.topic()).isEqualTo("express-delivery");
            assertThat(externalTask.retries()).isGreaterThanOrEqualTo(0);
        }

        // Verify postal delivery service task
        var postalDeliveryTask = processDefinition.activities().stream()
                .filter(activity -> "PostalDelivery".equals(activity.id()))
                .findFirst();
        assertThat(postalDeliveryTask).isPresent();
        assertThat(postalDeliveryTask.get().type()).isEqualTo(ActivityType.EXTERNAL_TASK);
        assertThat(postalDeliveryTask.get().name()).isEqualTo("Postal delivery");
        assertThat(postalDeliveryTask.get().parentId()).isNull();
        assertThat(postalDeliveryTask.get().incoming()).containsExactly("Gateway_1ob1wue");
        assertThat(postalDeliveryTask.get().outgoing()).containsExactly("Gateway_1kgd0tt");
        assertThat(postalDeliveryTask.get().inputs()).isEmpty();
        assertThat(postalDeliveryTask.get().outputs()).isEmpty();
        if (postalDeliveryTask.get() instanceof ExternalTask externalTask) {
            assertThat(externalTask.topic()).isEqualTo("postal-delivery");
            assertThat(externalTask.retries()).isGreaterThanOrEqualTo(0);
        }

        // Verify exclusive gateway (merge)
        var mergeGateway = processDefinition.activities().stream()
                .filter(activity -> "Gateway_1kgd0tt".equals(activity.id()))
                .findFirst();
        assertThat(mergeGateway).isPresent();
        assertThat(mergeGateway.get().type()).isEqualTo(ActivityType.EXCLUSIVE_GATEWAY);
        assertThat(mergeGateway.get().name()).isEmpty();
        assertThat(mergeGateway.get().parentId()).isNull();
        assertThat(mergeGateway.get().incoming()).containsExactlyInAnyOrder("CourierDelivery", "ExpressDelivery", "PostalDelivery");
        assertThat(mergeGateway.get().outgoing()).containsExactly("OrderDelivered");
        assertThat(mergeGateway.get().inputs()).isEmpty();
        assertThat(mergeGateway.get().outputs()).isEmpty();

        // Verify receive task
        var orderDeliveredTask = processDefinition.activities().stream()
                .filter(activity -> "OrderDelivered".equals(activity.id()))
                .findFirst();
        assertThat(orderDeliveredTask).isPresent();
        assertThat(orderDeliveredTask.get().type()).isEqualTo(ActivityType.RECEIVE_TASK);
        assertThat(orderDeliveredTask.get().name()).isEqualTo("Order delivered");
        assertThat(orderDeliveredTask.get().parentId()).isNull();
        assertThat(orderDeliveredTask.get().incoming()).containsExactly("Gateway_1kgd0tt");
        assertThat(orderDeliveredTask.get().outgoing()).containsExactly("Gateway_08m2xz9");
        assertThat(orderDeliveredTask.get().inputs()).isEmpty();
        assertThat(orderDeliveredTask.get().outputs()).isEmpty();
        if (orderDeliveredTask.get() instanceof MessageActivityDefinition messageActivity) {
            assertThat(messageActivity.messageReference()).isEqualTo("OrderDelivered");
        }

        // Verify inclusive gateway
        var inclusiveGateway = processDefinition.activities().stream()
                .filter(activity -> "Gateway_08m2xz9".equals(activity.id()))
                .findFirst();
        assertThat(inclusiveGateway).isPresent();
        assertThat(inclusiveGateway.get().type()).isEqualTo(ActivityType.INCLUSIVE_GATEWAY);
        assertThat(inclusiveGateway.get().name()).isEmpty();
        assertThat(inclusiveGateway.get().parentId()).isNull();
        assertThat(inclusiveGateway.get().incoming()).containsExactly("OrderDelivered");
        assertThat(inclusiveGateway.get().outgoing()).containsExactlyInAnyOrder("SellerNotificationOrderDelivered", "SellerNotificationOrderRejected", "ClientNotificationOrderRejected", "ClientNotificationOrderDelivered");
        assertThat(inclusiveGateway.get().inputs()).isEmpty();
        assertThat(inclusiveGateway.get().outputs()).isEmpty();

        // Verify seller notification (order delivered)
        var sellerNotificationDelivered = processDefinition.activities().stream()
                .filter(activity -> "SellerNotificationOrderDelivered".equals(activity.id()))
                .findFirst();
        assertThat(sellerNotificationDelivered).isPresent();
        assertThat(sellerNotificationDelivered.get().type()).isEqualTo(ActivityType.EXTERNAL_TASK);
        assertThat(sellerNotificationDelivered.get().name()).isEqualTo("Seller Notification (Order delivered)");
        assertThat(sellerNotificationDelivered.get().parentId()).isNull();
        assertThat(sellerNotificationDelivered.get().incoming()).containsExactly("Gateway_08m2xz9");
        assertThat(sellerNotificationDelivered.get().outgoing()).containsExactly("Event_12fvv8h");
        assertThat(sellerNotificationDelivered.get().inputs()).containsEntry("message", "${order.number} delivered.");
        assertThat(sellerNotificationDelivered.get().outputs()).isEmpty();
        if (sellerNotificationDelivered.get() instanceof ExternalTask externalTask) {
            assertThat(externalTask.topic()).isEqualTo("notification");
            assertThat(externalTask.retries()).isGreaterThanOrEqualTo(0);
        }

        // Verify client notification (order delivered)
        var clientNotificationDelivered = processDefinition.activities().stream()
                .filter(activity -> "ClientNotificationOrderDelivered".equals(activity.id()))
                .findFirst();
        assertThat(clientNotificationDelivered).isPresent();
        assertThat(clientNotificationDelivered.get().type()).isEqualTo(ActivityType.EXTERNAL_TASK);
        assertThat(clientNotificationDelivered.get().name()).isEqualTo("Client Notification (Order delivered)");
        assertThat(clientNotificationDelivered.get().parentId()).isNull();
        assertThat(clientNotificationDelivered.get().incoming()).containsExactly("Gateway_08m2xz9");
        assertThat(clientNotificationDelivered.get().outgoing()).containsExactly("Event_0kbk7ow");
        assertThat(clientNotificationDelivered.get().inputs()).containsEntry("message", "Hi ${client.firstName} ${client.lastName}! Thank you for your order!");
        assertThat(clientNotificationDelivered.get().outputs()).isEmpty();
        if (clientNotificationDelivered.get() instanceof ExternalTask externalTask) {
            assertThat(externalTask.topic()).isEqualTo("notification");
            assertThat(externalTask.retries()).isGreaterThanOrEqualTo(0);
        }

        // Verify seller notification (order rejected)
        var sellerNotificationRejected = processDefinition.activities().stream()
                .filter(activity -> "SellerNotificationOrderRejected".equals(activity.id()))
                .findFirst();
        assertThat(sellerNotificationRejected).isPresent();
        assertThat(sellerNotificationRejected.get().type()).isEqualTo(ActivityType.EXTERNAL_TASK);
        assertThat(sellerNotificationRejected.get().name()).isEqualTo("Seller Notification (Order rejected)");
        assertThat(sellerNotificationRejected.get().parentId()).isNull();
        assertThat(sellerNotificationRejected.get().incoming()).containsExactly("Gateway_08m2xz9");
        assertThat(sellerNotificationRejected.get().outgoing()).containsExactly("Event_0kbk7ow");
        assertThat(sellerNotificationRejected.get().inputs()).containsEntry("message", "${order.number} rejected.");
        assertThat(sellerNotificationRejected.get().outputs()).isEmpty();
        if (sellerNotificationRejected.get() instanceof ExternalTask externalTask) {
            assertThat(externalTask.topic()).isEqualTo("notification");
            assertThat(externalTask.retries()).isGreaterThanOrEqualTo(0);
        }

        // Verify client notification (order rejected)
        var clientNotificationRejected = processDefinition.activities().stream()
                .filter(activity -> "ClientNotificationOrderRejected".equals(activity.id()))
                .findFirst();
        assertThat(clientNotificationRejected).isPresent();
        assertThat(clientNotificationRejected.get().type()).isEqualTo(ActivityType.EXTERNAL_TASK);
        assertThat(clientNotificationRejected.get().name()).isEqualTo("Client Notification (Order rejected)");
        assertThat(clientNotificationRejected.get().parentId()).isNull();
        assertThat(clientNotificationRejected.get().incoming()).containsExactly("Gateway_08m2xz9");
        assertThat(clientNotificationRejected.get().outgoing()).containsExactly("Event_1jt8h16");
        assertThat(clientNotificationRejected.get().inputs()).containsEntry("message", "Hi ${client.firstName} ${client.lastName}! We regret that you refused the order :(");
        assertThat(clientNotificationRejected.get().outputs()).isEmpty();
        if (clientNotificationRejected.get() instanceof ExternalTask externalTask) {
            assertThat(externalTask.topic()).isEqualTo("notification");
            assertThat(externalTask.retries()).isGreaterThanOrEqualTo(0);
        }

        // Verify end event 1
        var endEvent1 = processDefinition.activities().stream()
                .filter(activity -> "Event_0kbk7ow".equals(activity.id()))
                .findFirst();
        assertThat(endEvent1).isPresent();
        assertThat(endEvent1.get().type()).isEqualTo(ActivityType.END_EVENT);
        assertThat(endEvent1.get().name()).isEmpty();
        assertThat(endEvent1.get().parentId()).isNull();
        assertThat(endEvent1.get().incoming()).containsExactlyInAnyOrder("ClientNotificationOrderDelivered", "SellerNotificationOrderRejected");
        assertThat(endEvent1.get().outgoing()).isEmpty();
        assertThat(endEvent1.get().inputs()).isEmpty();
        assertThat(endEvent1.get().outputs()).isEmpty();

        // Verify end event 2
        var endEvent2 = processDefinition.activities().stream()
                .filter(activity -> "Event_12fvv8h".equals(activity.id()))
                .findFirst();
        assertThat(endEvent2).isPresent();
        assertThat(endEvent2.get().type()).isEqualTo(ActivityType.END_EVENT);
        assertThat(endEvent2.get().name()).isEmpty();
        assertThat(endEvent2.get().parentId()).isNull();
        assertThat(endEvent2.get().incoming()).containsExactly("SellerNotificationOrderDelivered");
        assertThat(endEvent2.get().outgoing()).isEmpty();
        assertThat(endEvent2.get().inputs()).isEmpty();
        assertThat(endEvent2.get().outputs()).isEmpty();

        // Verify end event 3
        var endEvent3 = processDefinition.activities().stream()
                .filter(activity -> "Event_1jt8h16".equals(activity.id()))
                .findFirst();
        assertThat(endEvent3).isPresent();
        assertThat(endEvent3.get().type()).isEqualTo(ActivityType.END_EVENT);
        assertThat(endEvent3.get().name()).isEmpty();
        assertThat(endEvent3.get().parentId()).isNull();
        assertThat(endEvent3.get().incoming()).containsExactly("ClientNotificationOrderRejected");
        assertThat(endEvent3.get().outgoing()).isEmpty();
        assertThat(endEvent3.get().inputs()).isEmpty();
        assertThat(endEvent3.get().outputs()).isEmpty();

        // Verify total number of activities matches expected count
        assertThat(processDefinition.activities()).hasSize(15);
    }

    @Test
    @DisplayName("Should parse OrderPaymentProcess.bpmn")
    void shouldParseOrderPaymentProcess() {
        // Given
        var bpmnResource = new ClassPathResource(BPMN_FILES_PATH + "OrderPaymentProcess.bpmn");

        // When
        var processDefinition = bpmnParser.parse(bpmnResource);

        // Then - Process Definition basic fields
        assertThat(processDefinition.key()).isEqualTo("OrderPaymentProcess");
        assertThat(processDefinition.name()).isEqualTo("Order Payment Process");

        // Then - Metadata verification
        assertThat(processDefinition.metadata()).isNotNull();
        assertThat(processDefinition.metadata().origin()).isEqualTo(CAMUNDA_ORIGIN);
        assertThat(processDefinition.metadata().deployment()).isEqualTo("OrderPaymentProcess.bpmn");
        assertThat(processDefinition.metadata().schema()).isNotNull();
        assertThat(processDefinition.metadata().schema()).contains("OrderPaymentProcess");

        // Then - Messages verification
        assertThat(processDefinition.messages()).hasSize(2);
        assertThat(processDefinition.messages()).containsExactlyInAnyOrder("PaymentReceived", "PaymentTimeout");

        // Then - Errors verification
        assertThat(processDefinition.errors()).hasSize(3);
        assertThat(processDefinition.errors()).extracting("name").containsExactlyInAnyOrder("PaymentFailed", "PaymentRejected", "PaymentTimeout");

        // Then - Activities verification
        assertThat(processDefinition.activities()).isNotEmpty();

        // Verify start event
        var startEvent = processDefinition.activities().stream()
                .filter(activity -> "OrderPaymentProcessStartEvent".equals(activity.id()))
                .findFirst();
        assertThat(startEvent).isPresent();
        assertThat(startEvent.get().type()).isEqualTo(ActivityType.START_EVENT);
        assertThat(startEvent.get().name()).isEmpty();
        assertThat(startEvent.get().parentId()).isNull();
        assertThat(startEvent.get().incoming()).isEmpty();
        assertThat(startEvent.get().outgoing()).containsExactly("EventBasedGateway");
        assertThat(startEvent.get().inputs()).isEmpty();
        assertThat(startEvent.get().outputs()).isEmpty();

        // Verify event-based gateway
        var eventBasedGateway = processDefinition.activities().stream()
                .filter(activity -> "EventBasedGateway".equals(activity.id()))
                .findFirst();
        assertThat(eventBasedGateway).isPresent();
        assertThat(eventBasedGateway.get().type()).isEqualTo(ActivityType.EVENT_BASED_GATEWAY);
        assertThat(eventBasedGateway.get().name()).isEmpty();
        assertThat(eventBasedGateway.get().parentId()).isNull();
        assertThat(eventBasedGateway.get().incoming()).containsExactly("OrderPaymentProcessStartEvent");
        assertThat(eventBasedGateway.get().outgoing()).containsExactlyInAnyOrder("PaymentReceivedEvent", "PaymentRejectedEvent", "Event_1iboswq");
        assertThat(eventBasedGateway.get().inputs()).isEmpty();
        assertThat(eventBasedGateway.get().outputs()).isEmpty();

        // Verify payment received intermediate catch event
        var paymentReceivedEvent = processDefinition.activities().stream()
                .filter(activity -> "PaymentReceivedEvent".equals(activity.id()))
                .findFirst();
        assertThat(paymentReceivedEvent).isPresent();
        assertThat(paymentReceivedEvent.get().type()).isEqualTo(ActivityType.MESSAGE_INTERMEDIATE_CATCH_EVENT);
        assertThat(paymentReceivedEvent.get().name()).isEqualTo("Payment received event");
        assertThat(paymentReceivedEvent.get().parentId()).isNull();
        assertThat(paymentReceivedEvent.get().incoming()).containsExactly("EventBasedGateway");
        assertThat(paymentReceivedEvent.get().outgoing()).containsExactly("PaymentTransaction");
        assertThat(paymentReceivedEvent.get().inputs()).isEmpty();
        assertThat(paymentReceivedEvent.get().outputs()).isEmpty();
        if (paymentReceivedEvent.get() instanceof MessageActivityDefinition messageActivity) {
            assertThat(messageActivity.messageReference()).isEqualTo("PaymentReceived");
        }

        // Verify payment rejected intermediate catch event
        var paymentRejectedEvent = processDefinition.activities().stream()
                .filter(activity -> "PaymentRejectedEvent".equals(activity.id()))
                .findFirst();
        assertThat(paymentRejectedEvent).isPresent();
        assertThat(paymentRejectedEvent.get().type()).isEqualTo(ActivityType.INTERMEDIATE_CATCH_EVENT);
        assertThat(paymentRejectedEvent.get().name()).isEqualTo("Payment rejected event");
        assertThat(paymentRejectedEvent.get().parentId()).isNull();
        assertThat(paymentRejectedEvent.get().incoming()).containsExactly("EventBasedGateway");
        assertThat(paymentRejectedEvent.get().outgoing()).containsExactly("PaymentRejectedNotification");
        assertThat(paymentRejectedEvent.get().inputs()).isEmpty();
        assertThat(paymentRejectedEvent.get().outputs()).isEmpty();
        if (paymentRejectedEvent.get() instanceof ConditionalActivityDefinition conditionalActivity) {
            assertThat(conditionalActivity.condition()).isEqualTo("${paymentRejected==true}");
        }

        // Verify payment timeout intermediate catch event
        var paymentTimeoutEvent = processDefinition.activities().stream()
                .filter(activity -> "Event_1iboswq".equals(activity.id()))
                .findFirst();
        assertThat(paymentTimeoutEvent).isPresent();
        assertThat(paymentTimeoutEvent.get().type()).isEqualTo(ActivityType.MESSAGE_INTERMEDIATE_CATCH_EVENT);
        assertThat(paymentTimeoutEvent.get().name()).isEmpty();
        assertThat(paymentTimeoutEvent.get().parentId()).isNull();
        assertThat(paymentTimeoutEvent.get().incoming()).containsExactly("EventBasedGateway");
        assertThat(paymentTimeoutEvent.get().outgoing()).containsExactly("Event_1izsrul");
        assertThat(paymentTimeoutEvent.get().inputs()).isEmpty();
        assertThat(paymentTimeoutEvent.get().outputs()).isEmpty();
        if (paymentTimeoutEvent.get() instanceof MessageActivityDefinition messageActivity) {
            assertThat(messageActivity.messageReference()).isEqualTo("PaymentTimeout");
        }

        // Verify payment transaction subprocess
        var paymentTransaction = processDefinition.activities().stream()
                .filter(activity -> "PaymentTransaction".equals(activity.id()))
                .findFirst();
        assertThat(paymentTransaction).isPresent();
        assertThat(paymentTransaction.get().type()).isEqualTo(ActivityType.SUBPROCESS);
        assertThat(paymentTransaction.get().name()).isEqualTo("Payment Transaction");
        assertThat(paymentTransaction.get().parentId()).isNull();
        assertThat(paymentTransaction.get().incoming()).containsExactly("PaymentReceivedEvent");
        assertThat(paymentTransaction.get().outgoing()).containsExactly("ExclusiveGateway");
        assertThat(paymentTransaction.get().inputs()).isEmpty();
        assertThat(paymentTransaction.get().outputs()).isEmpty();

        // Verify payment transaction start event
        var paymentTransactionStartEvent = processDefinition.activities().stream()
                .filter(activity -> "PaymentTransactionStartEvent".equals(activity.id()))
                .findFirst();
        assertThat(paymentTransactionStartEvent).isPresent();
        assertThat(paymentTransactionStartEvent.get().type()).isEqualTo(ActivityType.START_EVENT);
        assertThat(paymentTransactionStartEvent.get().name()).isEmpty();
        assertThat(paymentTransactionStartEvent.get().parentId()).isEqualTo("PaymentTransaction");
        assertThat(paymentTransactionStartEvent.get().incoming()).isEmpty();
        assertThat(paymentTransactionStartEvent.get().outgoing()).containsExactly("Debit");
        assertThat(paymentTransactionStartEvent.get().inputs()).isEmpty();
        assertThat(paymentTransactionStartEvent.get().outputs()).isEmpty();

        // Verify debit service task
        var debitTask = processDefinition.activities().stream()
                .filter(activity -> "Debit".equals(activity.id()))
                .findFirst();
        assertThat(debitTask).isPresent();
        assertThat(debitTask.get().type()).isEqualTo(ActivityType.EXTERNAL_TASK);
        assertThat(debitTask.get().name()).isEqualTo("Debit");
        assertThat(debitTask.get().parentId()).isEqualTo("PaymentTransaction");
        assertThat(debitTask.get().incoming()).containsExactly("PaymentTransactionStartEvent");
        assertThat(debitTask.get().outgoing()).containsExactly("Deposit");
        assertThat(debitTask.get().inputs()).isEmpty();
        assertThat(debitTask.get().outputs()).isEmpty();
        if (debitTask.get() instanceof ExternalTask externalTask) {
            assertThat(externalTask.topic()).isEqualTo("debit");
            assertThat(externalTask.retries()).isGreaterThanOrEqualTo(0);
        }

        // Verify deposit service task
        var depositTask = processDefinition.activities().stream()
                .filter(activity -> "Deposit".equals(activity.id()))
                .findFirst();
        assertThat(depositTask).isPresent();
        assertThat(depositTask.get().type()).isEqualTo(ActivityType.EXTERNAL_TASK);
        assertThat(depositTask.get().name()).isEqualTo("Deposit");
        assertThat(depositTask.get().parentId()).isEqualTo("PaymentTransaction");
        assertThat(depositTask.get().incoming()).containsExactly("Debit");
        assertThat(depositTask.get().outgoing()).containsExactly("PaymentTransactionEndEvent");
        assertThat(depositTask.get().inputs()).isEmpty();
        assertThat(depositTask.get().outputs()).isEmpty();
        if (depositTask.get() instanceof ExternalTask externalTask) {
            assertThat(externalTask.topic()).isEqualTo("deposit");
            assertThat(externalTask.retries()).isGreaterThanOrEqualTo(0);
        }

        // Verify payment transaction end event
        var paymentTransactionEndEvent = processDefinition.activities().stream()
                .filter(activity -> "PaymentTransactionEndEvent".equals(activity.id()))
                .findFirst();
        assertThat(paymentTransactionEndEvent).isPresent();
        assertThat(paymentTransactionEndEvent.get().type()).isEqualTo(ActivityType.END_EVENT);
        assertThat(paymentTransactionEndEvent.get().name()).isEmpty();
        assertThat(paymentTransactionEndEvent.get().parentId()).isEqualTo("PaymentTransaction");
        assertThat(paymentTransactionEndEvent.get().incoming()).containsExactly("Deposit");
        assertThat(paymentTransactionEndEvent.get().outgoing()).isEmpty();
        assertThat(paymentTransactionEndEvent.get().inputs()).isEmpty();
        assertThat(paymentTransactionEndEvent.get().outputs()).isEmpty();

        // Verify exclusive gateway
        var exclusiveGateway = processDefinition.activities().stream()
                .filter(activity -> "ExclusiveGateway".equals(activity.id()))
                .findFirst();
        assertThat(exclusiveGateway).isPresent();
        assertThat(exclusiveGateway.get().type()).isEqualTo(ActivityType.EXCLUSIVE_GATEWAY);
        assertThat(exclusiveGateway.get().name()).isEmpty();
        assertThat(exclusiveGateway.get().parentId()).isNull();
        assertThat(exclusiveGateway.get().incoming()).containsExactly("PaymentTransaction");
        assertThat(exclusiveGateway.get().outgoing()).containsExactlyInAnyOrder("PaymentSuccessNotification", "ErrorEndEvent");
        assertThat(exclusiveGateway.get().inputs()).isEmpty();
        assertThat(exclusiveGateway.get().outputs()).isEmpty();

        // Verify payment success notification
        var paymentSuccessNotification = processDefinition.activities().stream()
                .filter(activity -> "PaymentSuccessNotification".equals(activity.id()))
                .findFirst();
        assertThat(paymentSuccessNotification).isPresent();
        assertThat(paymentSuccessNotification.get().type()).isEqualTo(ActivityType.EXTERNAL_TASK);
        assertThat(paymentSuccessNotification.get().name()).isEqualTo("Payment success notification");
        assertThat(paymentSuccessNotification.get().parentId()).isNull();
        assertThat(paymentSuccessNotification.get().incoming()).containsExactly("ExclusiveGateway");
        assertThat(paymentSuccessNotification.get().outgoing()).containsExactly("OrderPaymentProcessEndEvent");
        assertThat(paymentSuccessNotification.get().inputs()).containsEntry("message", "Hi, ${client.firstName} ${client.lastName}! Your payment accepted!");
        assertThat(paymentSuccessNotification.get().outputs()).isEmpty();
        if (paymentSuccessNotification.get() instanceof ExternalTask externalTask) {
            assertThat(externalTask.topic()).isEqualTo("notification");
            assertThat(externalTask.retries()).isGreaterThanOrEqualTo(0);
        }

        // Verify payment rejected notification
        var paymentRejectedNotification = processDefinition.activities().stream()
                .filter(activity -> "PaymentRejectedNotification".equals(activity.id()))
                .findFirst();
        assertThat(paymentRejectedNotification).isPresent();
        assertThat(paymentRejectedNotification.get().type()).isEqualTo(ActivityType.EXTERNAL_TASK);
        assertThat(paymentRejectedNotification.get().name()).isEqualTo("Payment rejected notification");
        assertThat(paymentRejectedNotification.get().parentId()).isNull();
        assertThat(paymentRejectedNotification.get().incoming()).containsExactly("PaymentRejectedEvent");
        assertThat(paymentRejectedNotification.get().outgoing()).containsExactly("Event_1eemimy");
        assertThat(paymentRejectedNotification.get().inputs()).containsEntry("message", "Hi, ${client.firstName} ${client.lastName}! Your payment rejected!");
        assertThat(paymentRejectedNotification.get().outputs()).isEmpty();
        if (paymentRejectedNotification.get() instanceof ExternalTask externalTask) {
            assertThat(externalTask.topic()).isEqualTo("notification");
            assertThat(externalTask.retries()).isGreaterThanOrEqualTo(0);
        }

        // Verify main process end event
        var mainProcessEndEvent = processDefinition.activities().stream()
                .filter(activity -> "OrderPaymentProcessEndEvent".equals(activity.id()))
                .findFirst();
        assertThat(mainProcessEndEvent).isPresent();
        assertThat(mainProcessEndEvent.get().type()).isEqualTo(ActivityType.END_EVENT);
        assertThat(mainProcessEndEvent.get().name()).isEmpty();
        assertThat(mainProcessEndEvent.get().parentId()).isNull();
        assertThat(mainProcessEndEvent.get().incoming()).containsExactly("PaymentSuccessNotification");
        assertThat(mainProcessEndEvent.get().outgoing()).isEmpty();
        assertThat(mainProcessEndEvent.get().inputs()).isEmpty();
        assertThat(mainProcessEndEvent.get().outputs()).isEmpty();

        // Verify error end event
        var errorEndEvent = processDefinition.activities().stream()
                .filter(activity -> "ErrorEndEvent".equals(activity.id()))
                .findFirst();
        assertThat(errorEndEvent).isPresent();
        assertThat(errorEndEvent.get().type()).isEqualTo(ActivityType.ERROR_END_EVENT);
        assertThat(errorEndEvent.get().name()).isEmpty();
        assertThat(errorEndEvent.get().parentId()).isNull();
        assertThat(errorEndEvent.get().incoming()).containsExactly("ExclusiveGateway");
        assertThat(errorEndEvent.get().outgoing()).isEmpty();
        assertThat(errorEndEvent.get().inputs()).isEmpty();
        assertThat(errorEndEvent.get().outputs()).isEmpty();

        // Verify payment rejected error end event
        var paymentRejectedErrorEndEvent = processDefinition.activities().stream()
                .filter(activity -> "Event_1eemimy".equals(activity.id()))
                .findFirst();
        assertThat(paymentRejectedErrorEndEvent).isPresent();
        assertThat(paymentRejectedErrorEndEvent.get().type()).isEqualTo(ActivityType.ERROR_END_EVENT);
        assertThat(paymentRejectedErrorEndEvent.get().name()).isEmpty();
        assertThat(paymentRejectedErrorEndEvent.get().parentId()).isNull();
        assertThat(paymentRejectedErrorEndEvent.get().incoming()).containsExactly("PaymentRejectedNotification");
        assertThat(paymentRejectedErrorEndEvent.get().outgoing()).isEmpty();
        assertThat(paymentRejectedErrorEndEvent.get().inputs()).isEmpty();
        assertThat(paymentRejectedErrorEndEvent.get().outputs()).isEmpty();

        // Verify payment timeout error end event
        var paymentTimeoutErrorEndEvent = processDefinition.activities().stream()
                .filter(activity -> "Event_1izsrul".equals(activity.id()))
                .findFirst();
        assertThat(paymentTimeoutErrorEndEvent).isPresent();
        assertThat(paymentTimeoutErrorEndEvent.get().type()).isEqualTo(ActivityType.ERROR_END_EVENT);
        assertThat(paymentTimeoutErrorEndEvent.get().name()).isEmpty();
        assertThat(paymentTimeoutErrorEndEvent.get().parentId()).isNull();
        assertThat(paymentTimeoutErrorEndEvent.get().incoming()).containsExactly("Event_1iboswq");
        assertThat(paymentTimeoutErrorEndEvent.get().outgoing()).isEmpty();
        assertThat(paymentTimeoutErrorEndEvent.get().inputs()).isEmpty();
        assertThat(paymentTimeoutErrorEndEvent.get().outputs()).isEmpty();

        // Verify payment failed event subprocess
        var paymentFailedSubprocess = processDefinition.activities().stream()
                .filter(activity -> "PaymentFailed".equals(activity.id()))
                .findFirst();
        assertThat(paymentFailedSubprocess).isPresent();
        assertThat(paymentFailedSubprocess.get().type()).isEqualTo(ActivityType.EVENT_SUBPROCESS);
        assertThat(paymentFailedSubprocess.get().name()).isEqualTo("Payment Failed");
        assertThat(paymentFailedSubprocess.get().parentId()).isNull();

        // Verify payment failed error start event
        var paymentFailedErrorStartEvent = processDefinition.activities().stream()
                .filter(activity -> "PaymentFailedErrorStartEvent".equals(activity.id()))
                .findFirst();
        assertThat(paymentFailedErrorStartEvent).isPresent();
        assertThat(paymentFailedErrorStartEvent.get().type()).isEqualTo(ActivityType.ERROR_START_EVENT);
        assertThat(paymentFailedErrorStartEvent.get().name()).isEmpty();
        assertThat(paymentFailedErrorStartEvent.get().parentId()).isEqualTo("PaymentFailed");
        assertThat(paymentFailedErrorStartEvent.get().incoming()).isEmpty();
        assertThat(paymentFailedErrorStartEvent.get().outgoing()).containsExactly("PaymentFailedNotification");
        assertThat(paymentFailedErrorStartEvent.get().inputs()).isEmpty();
        assertThat(paymentFailedErrorStartEvent.get().outputs()).isEmpty();

        // Verify payment failed notification
        var paymentFailedNotification = processDefinition.activities().stream()
                .filter(activity -> "PaymentFailedNotification".equals(activity.id()))
                .findFirst();
        assertThat(paymentFailedNotification).isPresent();
        assertThat(paymentFailedNotification.get().type()).isEqualTo(ActivityType.EXTERNAL_TASK);
        assertThat(paymentFailedNotification.get().name()).isEqualTo("Payment failed notification");
        assertThat(paymentFailedNotification.get().parentId()).isEqualTo("PaymentFailed");
        assertThat(paymentFailedNotification.get().incoming()).containsExactly("PaymentFailedErrorStartEvent");
        assertThat(paymentFailedNotification.get().outgoing()).containsExactly("PaymentFailedTerminateEndEvent");
        assertThat(paymentFailedNotification.get().inputs()).containsEntry("message", "Hi, ${client.firstName} ${client.lastName}! Your payment failed!");
        assertThat(paymentFailedNotification.get().outputs()).isEmpty();
        if (paymentFailedNotification.get() instanceof ExternalTask externalTask) {
            assertThat(externalTask.topic()).isEqualTo("notification");
            assertThat(externalTask.retries()).isGreaterThanOrEqualTo(0);
        }

        // Verify payment failed terminate end event
        var paymentFailedTerminateEndEvent = processDefinition.activities().stream()
                .filter(activity -> "PaymentFailedTerminateEndEvent".equals(activity.id()))
                .findFirst();
        assertThat(paymentFailedTerminateEndEvent).isPresent();
        assertThat(paymentFailedTerminateEndEvent.get().type()).isEqualTo(ActivityType.TERMINATE_END_EVENT);
        assertThat(paymentFailedTerminateEndEvent.get().name()).isEmpty();
        assertThat(paymentFailedTerminateEndEvent.get().parentId()).isEqualTo("PaymentFailed");
        assertThat(paymentFailedTerminateEndEvent.get().incoming()).containsExactly("PaymentFailedNotification");
        assertThat(paymentFailedTerminateEndEvent.get().outgoing()).isEmpty();
        assertThat(paymentFailedTerminateEndEvent.get().inputs()).isEmpty();
        assertThat(paymentFailedTerminateEndEvent.get().outputs()).isEmpty();

        // Verify total number of activities matches expected count
        assertThat(processDefinition.activities()).hasSize(21);
    }

    @Test
    @DisplayName("Should parse OrderSubmittedProcess.bpmn")
    void shouldParseOrderSubmittedProcess() {
        // Given
        var bpmnResource = new ClassPathResource(BPMN_FILES_PATH + "OrderSubmittedProcess.bpmn");

        // When
        var processDefinition = bpmnParser.parse(bpmnResource);

        // Then - Process Definition basic fields
        assertThat(processDefinition.key()).isEqualTo("OrderSubmittedProcess");
        assertThat(processDefinition.name()).isEqualTo("Order Submitted Process");

        // Then - Metadata verification
        assertThat(processDefinition.metadata()).isNotNull();
        assertThat(processDefinition.metadata().origin()).isEqualTo(CAMUNDA_ORIGIN);
        assertThat(processDefinition.metadata().deployment()).isEqualTo("OrderSubmittedProcess.bpmn");
        assertThat(processDefinition.metadata().schema()).isNotNull();
        assertThat(processDefinition.metadata().schema()).contains("OrderSubmittedProcess");

        // Then - Messages verification (should be empty for this BPMN)
        assertThat(processDefinition.messages()).isEmpty();

        // Then - Errors verification (should be empty for this BPMN)
        assertThat(processDefinition.errors()).isEmpty();

        // Then - Activities verification
        assertThat(processDefinition.activities()).isNotEmpty();

        // Verify start event
        var startEvent = processDefinition.activities().stream()
                .filter(activity -> "OrderSubmittedProcessStartEvent".equals(activity.id()))
                .findFirst();
        assertThat(startEvent).isPresent();
        assertThat(startEvent.get().type()).isEqualTo(ActivityType.START_EVENT);
        assertThat(startEvent.get().name()).isEmpty();
        assertThat(startEvent.get().parentId()).isNull();
        assertThat(startEvent.get().incoming()).isEmpty();
        assertThat(startEvent.get().outgoing()).containsExactly("Gateway_0t354xy");
        assertThat(startEvent.get().inputs()).isEmpty();
        assertThat(startEvent.get().outputs()).isEmpty();

        // Verify parallel gateway (split)
        var parallelGatewaySplit = processDefinition.activities().stream()
                .filter(activity -> "Gateway_0t354xy".equals(activity.id()))
                .findFirst();
        assertThat(parallelGatewaySplit).isPresent();
        assertThat(parallelGatewaySplit.get().type()).isEqualTo(ActivityType.PARALLEL_GATEWAY);
        assertThat(parallelGatewaySplit.get().name()).isEmpty();
        assertThat(parallelGatewaySplit.get().parentId()).isNull();
        assertThat(parallelGatewaySplit.get().incoming()).containsExactly("OrderSubmittedProcessStartEvent");
        assertThat(parallelGatewaySplit.get().outgoing()).containsExactlyInAnyOrder("NotificationToClient", "NotificationToSeller");
        assertThat(parallelGatewaySplit.get().inputs()).isEmpty();
        assertThat(parallelGatewaySplit.get().outputs()).isEmpty();

        // Verify notification to client send task
        var notificationToClient = processDefinition.activities().stream()
                .filter(activity -> "NotificationToClient".equals(activity.id()))
                .findFirst();
        assertThat(notificationToClient).isPresent();
        assertThat(notificationToClient.get().type()).isEqualTo(ActivityType.EXTERNAL_TASK);
        assertThat(notificationToClient.get().name()).isEqualTo("Notification to client");
        assertThat(notificationToClient.get().parentId()).isNull();
        assertThat(notificationToClient.get().incoming()).containsExactly("Gateway_0t354xy");
        assertThat(notificationToClient.get().outgoing()).containsExactly("Gateway_0yprn2c");
        assertThat(notificationToClient.get().inputs()).containsEntry("message", "Hi ${client.firstName} ${client.lastName}! Your order ${order.number} accepted!");
        assertThat(notificationToClient.get().outputs()).isEmpty();
        if (notificationToClient.get() instanceof ExternalTask externalTask) {
            assertThat(externalTask.topic()).isEqualTo("notification");
            assertThat(externalTask.retries()).isGreaterThanOrEqualTo(0);
        }

        // Verify notification to seller send task
        var notificationToSeller = processDefinition.activities().stream()
                .filter(activity -> "NotificationToSeller".equals(activity.id()))
                .findFirst();
        assertThat(notificationToSeller).isPresent();
        assertThat(notificationToSeller.get().type()).isEqualTo(ActivityType.EXTERNAL_TASK);
        assertThat(notificationToSeller.get().name()).isEqualTo("Notification to seller");
        assertThat(notificationToSeller.get().parentId()).isNull();
        assertThat(notificationToSeller.get().incoming()).containsExactly("Gateway_0t354xy");
        assertThat(notificationToSeller.get().outgoing()).containsExactly("Gateway_0yprn2c");
        assertThat(notificationToSeller.get().inputs()).containsEntry("message", "New order ${order.number} created.");
        assertThat(notificationToSeller.get().outputs()).isEmpty();
        if (notificationToSeller.get() instanceof ExternalTask externalTask) {
            assertThat(externalTask.topic()).isEqualTo("notification");
            assertThat(externalTask.retries()).isGreaterThanOrEqualTo(0);
        }

        // Verify parallel gateway (join)
        var parallelGatewayJoin = processDefinition.activities().stream()
                .filter(activity -> "Gateway_0yprn2c".equals(activity.id()))
                .findFirst();
        assertThat(parallelGatewayJoin).isPresent();
        assertThat(parallelGatewayJoin.get().type()).isEqualTo(ActivityType.PARALLEL_GATEWAY);
        assertThat(parallelGatewayJoin.get().name()).isEmpty();
        assertThat(parallelGatewayJoin.get().parentId()).isNull();
        assertThat(parallelGatewayJoin.get().incoming()).containsExactlyInAnyOrder("NotificationToClient", "NotificationToSeller");
        assertThat(parallelGatewayJoin.get().outgoing()).containsExactly("OrderSubmittedProcessEndEvent");
        assertThat(parallelGatewayJoin.get().inputs()).isEmpty();
        assertThat(parallelGatewayJoin.get().outputs()).isEmpty();

        // Verify end event
        var endEvent = processDefinition.activities().stream()
                .filter(activity -> "OrderSubmittedProcessEndEvent".equals(activity.id()))
                .findFirst();
        assertThat(endEvent).isPresent();
        assertThat(endEvent.get().type()).isEqualTo(ActivityType.END_EVENT);
        assertThat(endEvent.get().name()).isEmpty();
        assertThat(endEvent.get().parentId()).isNull();
        assertThat(endEvent.get().incoming()).containsExactly("Gateway_0yprn2c");
        assertThat(endEvent.get().outgoing()).isEmpty();
        assertThat(endEvent.get().inputs()).isEmpty();
        assertThat(endEvent.get().outputs()).isEmpty();

        // Verify total number of activities matches expected count
        assertThat(processDefinition.activities()).hasSize(6);
    }

    @Test
    @DisplayName("Should parse OrderProcess.bpmn")
    void shouldParseOrderProcess() {
        // Given
        var bpmnResource = new ClassPathResource(BPMN_FILES_PATH + "OrderProcess.bpmn");

        // When
        var processDefinition = bpmnParser.parse(bpmnResource);

        // Then - Process Definition basic fields
        assertThat(processDefinition.key()).isEqualTo("OrderProcess");
        assertThat(processDefinition.name()).isEqualTo("Order Process");

        // Then - Metadata verification
        assertThat(processDefinition.metadata()).isNotNull();
        assertThat(processDefinition.metadata().origin()).isEqualTo(CAMUNDA_ORIGIN);
        assertThat(processDefinition.metadata().deployment()).isEqualTo("OrderProcess.bpmn");
        assertThat(processDefinition.metadata().schema()).isNotNull();
        assertThat(processDefinition.metadata().schema()).contains("OrderProcess");

        // Then - Messages verification
        assertThat(processDefinition.messages()).hasSize(1);
        assertThat(processDefinition.messages()).contains("PaymentCancel");

        // Then - Errors verification
        assertThat(processDefinition.errors()).hasSize(2);
        assertThat(processDefinition.errors()).extracting("name").containsExactlyInAnyOrder("PaymentRejected", "PaymentTimeout");

        // Then - Activities verification
        assertThat(processDefinition.activities()).isNotEmpty();

        // Verify start event
        var startEvent = processDefinition.activities().stream()
                .filter(activity -> "OrderProcessStartEvent".equals(activity.id()))
                .findFirst();
        assertThat(startEvent).isPresent();
        assertThat(startEvent.get().type()).isEqualTo(ActivityType.START_EVENT);
        assertThat(startEvent.get().name()).isEmpty();
        assertThat(startEvent.get().parentId()).isNull();
        assertThat(startEvent.get().incoming()).isEmpty();
        assertThat(startEvent.get().outgoing()).containsExactly("OrderSubmittedProcess");
        assertThat(startEvent.get().inputs()).isEmpty();
        assertThat(startEvent.get().outputs()).isEmpty();

        // Verify OrderSubmittedProcess call activity
        var orderSubmittedProcess = processDefinition.activities().stream()
                .filter(activity -> "OrderSubmittedProcess".equals(activity.id()))
                .findFirst();
        assertThat(orderSubmittedProcess).isPresent();
        assertThat(orderSubmittedProcess.get().type()).isEqualTo(ActivityType.CALL_ACTIVITY);
        assertThat(orderSubmittedProcess.get().name()).isEqualTo("Order Submitted Process");
        assertThat(orderSubmittedProcess.get().parentId()).isNull();
        assertThat(orderSubmittedProcess.get().incoming()).containsExactly("OrderProcessStartEvent");
        assertThat(orderSubmittedProcess.get().outgoing()).containsExactly("OrderPaymentProcess");
        assertThat(orderSubmittedProcess.get().inputs()).isEmpty();
        assertThat(orderSubmittedProcess.get().outputs()).isEmpty();
        if (orderSubmittedProcess.get() instanceof CallActivity callActivity) {
            assertThat(callActivity.calledElement()).isEqualTo("OrderSubmittedProcess");
        }

        // Verify OrderPaymentProcess call activity
        var orderPaymentProcess = processDefinition.activities().stream()
                .filter(activity -> "OrderPaymentProcess".equals(activity.id()))
                .findFirst();
        assertThat(orderPaymentProcess).isPresent();
        assertThat(orderPaymentProcess.get().type()).isEqualTo(ActivityType.CALL_ACTIVITY);
        assertThat(orderPaymentProcess.get().name()).isEqualTo("Order Payment Process");
        assertThat(orderPaymentProcess.get().parentId()).isNull();
        assertThat(orderPaymentProcess.get().incoming()).containsExactly("OrderSubmittedProcess");
        assertThat(orderPaymentProcess.get().outgoing()).containsExactly("OrderProcessEndEvent");
        assertThat(orderPaymentProcess.get().inputs()).isEmpty();
        assertThat(orderPaymentProcess.get().outputs()).isEmpty();
        if (orderPaymentProcess.get() instanceof CallActivity callActivity) {
            assertThat(callActivity.calledElement()).isEqualTo("OrderPaymentProcess");
        }

        // Verify main process end event
        var mainEndEvent = processDefinition.activities().stream()
                .filter(activity -> "OrderProcessEndEvent".equals(activity.id()))
                .findFirst();
        assertThat(mainEndEvent).isPresent();
        assertThat(mainEndEvent.get().type()).isEqualTo(ActivityType.END_EVENT);
        assertThat(mainEndEvent.get().name()).isEmpty();
        assertThat(mainEndEvent.get().parentId()).isNull();
        assertThat(mainEndEvent.get().incoming()).containsExactly("OrderPaymentProcess");
        assertThat(mainEndEvent.get().outgoing()).isEmpty();
        assertThat(mainEndEvent.get().inputs()).isEmpty();
        assertThat(mainEndEvent.get().outputs()).isEmpty();

        // Verify payment cancel message boundary event
        var paymentCancelBoundaryEvent = processDefinition.activities().stream()
                .filter(activity -> "Event_1gm5bl0".equals(activity.id()))
                .findFirst();
        assertThat(paymentCancelBoundaryEvent).isPresent();
        assertThat(paymentCancelBoundaryEvent.get().type()).isEqualTo(ActivityType.MESSAGE_BOUNDARY_EVENT);
        assertThat(paymentCancelBoundaryEvent.get().name()).isEqualTo("Payment cancel event");
        assertThat(paymentCancelBoundaryEvent.get().parentId()).isNull();
        assertThat(paymentCancelBoundaryEvent.get().incoming()).isEmpty();
        assertThat(paymentCancelBoundaryEvent.get().outgoing()).containsExactly("PaymentCanceledNotification");
        assertThat(paymentCancelBoundaryEvent.get().inputs()).isEmpty();
        assertThat(paymentCancelBoundaryEvent.get().outputs()).isEmpty();
        if (paymentCancelBoundaryEvent.get() instanceof MessageActivityDefinition messageActivity) {
            assertThat(messageActivity.messageReference()).isEqualTo("PaymentCancel");
        }

        // Verify payment timeout error boundary event
        var paymentTimeoutBoundaryEvent = processDefinition.activities().stream()
                .filter(activity -> "Event_0d1wliw".equals(activity.id()))
                .findFirst();
        assertThat(paymentTimeoutBoundaryEvent).isPresent();
        assertThat(paymentTimeoutBoundaryEvent.get().type()).isEqualTo(ActivityType.ERROR_BOUNDARY_EVENT);
        assertThat(paymentTimeoutBoundaryEvent.get().name()).isEmpty();
        assertThat(paymentTimeoutBoundaryEvent.get().parentId()).isNull();
        assertThat(paymentTimeoutBoundaryEvent.get().incoming()).isEmpty();
        assertThat(paymentTimeoutBoundaryEvent.get().outgoing()).containsExactly("PaymentTimeoutNotification");
        assertThat(paymentTimeoutBoundaryEvent.get().inputs()).isEmpty();
        assertThat(paymentTimeoutBoundaryEvent.get().outputs()).isEmpty();

        // Verify payment canceled notification send task
        var paymentCanceledNotification = processDefinition.activities().stream()
                .filter(activity -> "PaymentCanceledNotification".equals(activity.id()))
                .findFirst();
        assertThat(paymentCanceledNotification).isPresent();
        assertThat(paymentCanceledNotification.get().type()).isEqualTo(ActivityType.EXTERNAL_TASK);
        assertThat(paymentCanceledNotification.get().name()).isEqualTo("Payment canceled notification");
        assertThat(paymentCanceledNotification.get().parentId()).isNull();
        assertThat(paymentCanceledNotification.get().incoming()).containsExactly("Event_1gm5bl0");
        assertThat(paymentCanceledNotification.get().outgoing()).containsExactly("Event_1hvxg1f");
        assertThat(paymentCanceledNotification.get().inputs()).containsEntry("message", "Hi, ${client.firstName} ${client.lastName}! Your payment canceled!");
        assertThat(paymentCanceledNotification.get().outputs()).isEmpty();
        if (paymentCanceledNotification.get() instanceof ExternalTask externalTask) {
            assertThat(externalTask.topic()).isEqualTo("notification");
            assertThat(externalTask.retries()).isGreaterThanOrEqualTo(0);
        }

        // Verify payment timeout notification send task
        var paymentTimeoutNotification = processDefinition.activities().stream()
                .filter(activity -> "PaymentTimeoutNotification".equals(activity.id()))
                .findFirst();
        assertThat(paymentTimeoutNotification).isPresent();
        assertThat(paymentTimeoutNotification.get().type()).isEqualTo(ActivityType.EXTERNAL_TASK);
        assertThat(paymentTimeoutNotification.get().name()).isEqualTo("Payment timeout notification");
        assertThat(paymentTimeoutNotification.get().parentId()).isNull();
        assertThat(paymentTimeoutNotification.get().incoming()).containsExactly("Event_0d1wliw");
        assertThat(paymentTimeoutNotification.get().outgoing()).containsExactly("Event_1jr9dj4");
        assertThat(paymentTimeoutNotification.get().inputs()).containsEntry("message", "Hi, ${client.firstName} ${client.lastName}! Your payment canceled due to timeout!");
        assertThat(paymentTimeoutNotification.get().outputs()).isEmpty();
        if (paymentTimeoutNotification.get() instanceof ExternalTask externalTask) {
            assertThat(externalTask.topic()).isEqualTo("notification");
            assertThat(externalTask.retries()).isGreaterThanOrEqualTo(0);
        }

        // Verify payment canceled end event
        var paymentCanceledEndEvent = processDefinition.activities().stream()
                .filter(activity -> "Event_1hvxg1f".equals(activity.id()))
                .findFirst();
        assertThat(paymentCanceledEndEvent).isPresent();
        assertThat(paymentCanceledEndEvent.get().type()).isEqualTo(ActivityType.END_EVENT);
        assertThat(paymentCanceledEndEvent.get().name()).isEmpty();
        assertThat(paymentCanceledEndEvent.get().parentId()).isNull();
        assertThat(paymentCanceledEndEvent.get().incoming()).containsExactly("PaymentCanceledNotification");
        assertThat(paymentCanceledEndEvent.get().outgoing()).isEmpty();
        assertThat(paymentCanceledEndEvent.get().inputs()).isEmpty();
        assertThat(paymentCanceledEndEvent.get().outputs()).isEmpty();

        // Verify payment timeout end event
        var paymentTimeoutEndEvent = processDefinition.activities().stream()
                .filter(activity -> "Event_1jr9dj4".equals(activity.id()))
                .findFirst();
        assertThat(paymentTimeoutEndEvent).isPresent();
        assertThat(paymentTimeoutEndEvent.get().type()).isEqualTo(ActivityType.END_EVENT);
        assertThat(paymentTimeoutEndEvent.get().name()).isEmpty();
        assertThat(paymentTimeoutEndEvent.get().parentId()).isNull();
        assertThat(paymentTimeoutEndEvent.get().incoming()).containsExactly("PaymentTimeoutNotification");
        assertThat(paymentTimeoutEndEvent.get().outgoing()).isEmpty();
        assertThat(paymentTimeoutEndEvent.get().inputs()).isEmpty();
        assertThat(paymentTimeoutEndEvent.get().outputs()).isEmpty();

        // Verify payment failed event subprocess
        var paymentFailedSubprocess = processDefinition.activities().stream()
                .filter(activity -> "PaymentFailed".equals(activity.id()))
                .findFirst();
        assertThat(paymentFailedSubprocess).isPresent();
        assertThat(paymentFailedSubprocess.get().type()).isEqualTo(ActivityType.EVENT_SUBPROCESS);
        assertThat(paymentFailedSubprocess.get().name()).isEqualTo("Payment Rejected");
        assertThat(paymentFailedSubprocess.get().parentId()).isNull();

        // Verify payment failed error start event
        var paymentFailedErrorStartEvent = processDefinition.activities().stream()
                .filter(activity -> "PaymentFailedErrorStartEvent".equals(activity.id()))
                .findFirst();
        assertThat(paymentFailedErrorStartEvent).isPresent();
        assertThat(paymentFailedErrorStartEvent.get().type()).isEqualTo(ActivityType.ERROR_START_EVENT);
        assertThat(paymentFailedErrorStartEvent.get().name()).isEmpty();
        assertThat(paymentFailedErrorStartEvent.get().parentId()).isEqualTo("PaymentFailed");
        assertThat(paymentFailedErrorStartEvent.get().incoming()).isEmpty();
        assertThat(paymentFailedErrorStartEvent.get().outgoing()).containsExactly("PaymentRejectedNotification");
        assertThat(paymentFailedErrorStartEvent.get().inputs()).isEmpty();
        assertThat(paymentFailedErrorStartEvent.get().outputs()).isEmpty();

        // Verify payment rejected notification send task (within subprocess)
        var paymentRejectedNotification = processDefinition.activities().stream()
                .filter(activity -> "PaymentRejectedNotification".equals(activity.id()))
                .findFirst();
        assertThat(paymentRejectedNotification).isPresent();
        assertThat(paymentRejectedNotification.get().type()).isEqualTo(ActivityType.EXTERNAL_TASK);
        assertThat(paymentRejectedNotification.get().name()).isEqualTo("Payment rejected notification");
        assertThat(paymentRejectedNotification.get().parentId()).isEqualTo("PaymentFailed");
        assertThat(paymentRejectedNotification.get().incoming()).containsExactly("PaymentFailedErrorStartEvent");
        assertThat(paymentRejectedNotification.get().outgoing()).containsExactly("PaymentFailedTerminateEndEvent");
        assertThat(paymentRejectedNotification.get().inputs()).containsEntry("message", "Hi, ${client.firstName} ${client.lastName}! Your payment failed!");
        assertThat(paymentRejectedNotification.get().outputs()).isEmpty();
        if (paymentRejectedNotification.get() instanceof ExternalTask externalTask) {
            assertThat(externalTask.topic()).isEqualTo("notification");
            assertThat(externalTask.retries()).isGreaterThanOrEqualTo(0);
        }

        // Verify payment failed terminate end event
        var paymentFailedTerminateEndEvent = processDefinition.activities().stream()
                .filter(activity -> "PaymentFailedTerminateEndEvent".equals(activity.id()))
                .findFirst();
        assertThat(paymentFailedTerminateEndEvent).isPresent();
        assertThat(paymentFailedTerminateEndEvent.get().type()).isEqualTo(ActivityType.TERMINATE_END_EVENT);
        assertThat(paymentFailedTerminateEndEvent.get().name()).isEmpty();
        assertThat(paymentFailedTerminateEndEvent.get().parentId()).isEqualTo("PaymentFailed");
        assertThat(paymentFailedTerminateEndEvent.get().incoming()).containsExactly("PaymentRejectedNotification");
        assertThat(paymentFailedTerminateEndEvent.get().outgoing()).isEmpty();
        assertThat(paymentFailedTerminateEndEvent.get().inputs()).isEmpty();
        assertThat(paymentFailedTerminateEndEvent.get().outputs()).isEmpty();

        // Verify total number of activities matches expected count
        assertThat(processDefinition.activities()).hasSize(14);
    }

}