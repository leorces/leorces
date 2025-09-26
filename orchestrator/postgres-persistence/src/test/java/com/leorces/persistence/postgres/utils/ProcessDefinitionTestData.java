package com.leorces.persistence.postgres.utils;

import com.leorces.model.definition.ProcessDefinition;
import com.leorces.model.definition.ProcessDefinitionMetadata;
import com.leorces.model.definition.activity.event.EndEvent;
import com.leorces.model.definition.activity.event.IntermediateCatchEvent;
import com.leorces.model.definition.activity.event.MessageStartEvent;
import com.leorces.model.definition.activity.event.StartEvent;
import com.leorces.model.definition.activity.gateway.ParallelGateway;
import com.leorces.model.definition.activity.subprocess.EventSubprocess;
import com.leorces.model.definition.activity.task.ExternalTask;

import java.util.List;
import java.util.Map;

public class ProcessDefinitionTestData {

    public static ProcessDefinition createOrderFulfillmentProcessDefinition() {
        var orderFulfillmentProcessStartEvent = StartEvent.builder()
                .id("OrderFulfillmentProcessStartEvent")
                .name("")
                .parentId(null)
                .incoming(List.of())
                .outgoing(List.of("OrderFulfillmentNotification"))
                .build();

        var orderFulfillmentProcessEndEvent = EndEvent.builder()
                .id("OrderFulfillmentProcessEndEvent")
                .name("")
                .parentId(null)
                .incoming(List.of("OrderFulfillmentNotificationFinishedNotification"))
                .outgoing(List.of())
                .build();

        var waitOrderFulfillment = IntermediateCatchEvent.builder()
                .id("WaitOrderFulfillment")
                .name("")
                .parentId(null)
                .condition("${orderFulfilled==true}")
                .variableName(null)
                .variableEvents(null)
                .incoming(List.of("OrderFulfillmentNotification"))
                .outgoing(List.of("OrderFulfillmentNotificationFinishedNotification"))
                .inputs(Map.of())
                .outputs(Map.of())
                .build();

        var orderFulfillmentNotificationFinishedNotification = ExternalTask.builder()
                .id("OrderFulfillmentNotificationFinishedNotification")
                .name("Order fulfillment finished notification")
                .parentId(null)
                .topic("notification")
                .retries(3)
                .incoming(List.of("WaitOrderFulfillment"))
                .outgoing(List.of("OrderFulfillmentProcessEndEvent"))
                .inputs(Map.of("message", "Hi ${client.firstName} ${client.lastName}! Your order ${order.number} fullfiled! Will send it soon!"))
                .outputs(Map.of())
                .build();

        var orderFulfillmentNotification = ExternalTask.builder()
                .id("OrderFulfillmentNotification")
                .name("Order fulfillment notification")
                .parentId(null)
                .topic("notification")
                .retries(3)
                .incoming(List.of("OrderFulfillmentProcessStartEvent"))
                .outgoing(List.of("WaitOrderFulfillment"))
                .inputs(Map.of("message", "Hi, ${client.firstName} ${client.lastName}! Your order ${order.number} will be fulfilled soon!"))
                .outputs(Map.of())
                .build();

        var orderFulfillment = EventSubprocess.builder()
                .id("OrderFulfillment")
                .name("Order fulfillment")
                .parentId(null)
                .incoming(List.of())
                .outgoing(List.of())
                .build();

        var orderFulfillmentStartEvent = MessageStartEvent.builder()
                .id("OrderFulfillmentStartEvent")
                .name("")
                .parentId("OrderFulfillment")
                .incoming(List.of())
                .outgoing(List.of("ProcessOrderFulfillment"))
                .messageReference("Message_2h754qt")
                .build();

        var orderFulfillmentEndEvent = EndEvent.builder()
                .id("OrderFulfillmentEndEvent")
                .name("")
                .parentId("OrderFulfillment")
                .incoming(List.of("ProcessOrderFulfillment"))
                .outgoing(List.of())
                .build();

        var processOrderFulfillment = ExternalTask.builder()
                .id("ProcessOrderFulfillment")
                .name("Process order fulfillment")
                .parentId("OrderFulfillment")
                .topic("process-order-fulfillment")
                .retries(3)
                .incoming(List.of("OrderFulfillmentStartEvent"))
                .outgoing(List.of("OrderFulfillmentEndEvent"))
                .inputs(Map.of())
                .outputs(Map.of("orderFulfilled", "true"))
                .build();

        return ProcessDefinition.builder()
                .key("order-fulfillment-process")
                .name("Order Fulfillment Process")
                .version(1)
                .messages(List.of("OrderFulfillmentFinishedMessage"))
                .errors(List.of())
                .activities(List.of(
                        orderFulfillmentProcessStartEvent,
                        orderFulfillmentProcessEndEvent,
                        waitOrderFulfillment,
                        orderFulfillmentNotificationFinishedNotification,
                        orderFulfillmentNotification,
                        orderFulfillment,
                        orderFulfillmentStartEvent,
                        orderFulfillmentEndEvent,
                        processOrderFulfillment
                ))
                .metadata(ProcessDefinitionMetadata.builder()
                        .schema("test-schema")
                        .origin("test")
                        .deployment("test-deployment")
                        .build())
                .build();
    }

    public static ProcessDefinition createOrderSubmittedProcessDefinition() {
        var orderSubmittedProcessStartEvent = StartEvent.builder()
                .id("OrderSubmittedProcessStartEvent")
                .name("")
                .parentId(null)
                .incoming(List.of())
                .outgoing(List.of("Gateway_0t354xy"))
                .build();

        var orderSubmittedProcessEndEvent = EndEvent.builder()
                .id("OrderSubmittedProcessEndEvent")
                .name("")
                .parentId(null)
                .incoming(List.of("Gateway_0yprn2c"))
                .outgoing(List.of())
                .build();

        var notificationToClient = ExternalTask.builder()
                .id("NotificationToClient")
                .name("Notification to client")
                .parentId(null)
                .topic("notification")
                .retries(3)
                .incoming(List.of("Gateway_0t354xy"))
                .outgoing(List.of("Gateway_0yprn2c"))
                .inputs(Map.of("message", "Hi ${client.firstName} ${client.lastName}! Your order ${order.number} accepted!"))
                .outputs(Map.of())
                .build();

        var notificationToSeller = ExternalTask.builder()
                .id("NotificationToSeller")
                .name("Notification to seller")
                .parentId(null)
                .topic("notification")
                .retries(3)
                .incoming(List.of("Gateway_0t354xy"))
                .outgoing(List.of("Gateway_0yprn2c"))
                .inputs(Map.of("message", "New order ${order.number} created."))
                .outputs(Map.of())
                .build();

        var gatewayStart = ParallelGateway.builder()
                .id("Gateway_0t354xy")
                .name("")
                .parentId(null)
                .incoming(List.of("OrderSubmittedProcessStartEvent"))
                .outgoing(List.of("NotificationToClient", "NotificationToSeller"))
                .build();

        var gatewayEnd = ParallelGateway.builder()
                .id("Gateway_0yprn2c")
                .name("")
                .parentId(null)
                .incoming(List.of("NotificationToSeller", "NotificationToClient"))
                .outgoing(List.of("OrderSubmittedProcessEndEvent"))
                .build();

        return ProcessDefinition.builder()
                .key("order-submitted-process")
                .name("Order Submitted Process")
                .version(1)
                .messages(List.of())
                .errors(List.of())
                .activities(List.of(
                        orderSubmittedProcessStartEvent,
                        orderSubmittedProcessEndEvent,
                        notificationToClient,
                        notificationToSeller,
                        gatewayStart,
                        gatewayEnd
                ))
                .metadata(ProcessDefinitionMetadata.builder()
                        .schema("test-schema")
                        .origin("test")
                        .deployment("test-deployment")
                        .build())
                .build();
    }

}
