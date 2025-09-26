package com.leorces.persistence.postgres.utils;

import com.leorces.model.runtime.activity.ActivityExecution;
import com.leorces.model.runtime.process.Process;

import java.util.List;

public class ActivityTestData {

    public static ActivityExecution createNotificationToClientActivityExecution(Process process) {
        var orderVariable = VariableTestData.createOrderVariable();
        var clientVariable = VariableTestData.createClientVariable();
        return ActivityExecution.builder()
                .definitionId("NotificationToClient")
                .process(process)
                .variables(List.of(orderVariable, clientVariable))
                .retries(3)
                .build();
    }

    public static ActivityExecution createNotificationToSellerActivityExecution(Process process) {
        var orderVariable = VariableTestData.createOrderVariable();
        var clientVariable = VariableTestData.createClientVariable();
        return ActivityExecution.builder()
                .definitionId("NotificationToSeller")
                .process(process)
                .variables(List.of(orderVariable, clientVariable))
                .retries(3)
                .build();
    }

    public static ActivityExecution createOrderFulfillmentNotificationActivityExecution(Process process) {
        var orderVariable = VariableTestData.createOrderVariable();
        var clientVariable = VariableTestData.createClientVariable();
        return ActivityExecution.builder()
                .definitionId("OrderFulfillmentNotification")
                .process(process)
                .variables(List.of(orderVariable, clientVariable))
                .retries(3)
                .build();
    }

}
