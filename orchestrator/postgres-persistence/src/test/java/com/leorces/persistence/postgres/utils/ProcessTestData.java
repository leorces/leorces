package com.leorces.persistence.postgres.utils;

import com.leorces.model.definition.ProcessDefinition;
import com.leorces.model.runtime.process.Process;

import java.util.List;

public class ProcessTestData {

    public static Process createOrderSubmittedProcess(ProcessDefinition processDefinition) {
        var orderVariable = VariableTestData.createOrderVariable();
        var clientVariable = VariableTestData.createClientVariable();
        return Process.builder()
                .definition(processDefinition)
                .variables(List.of(orderVariable, clientVariable))
                .build();
    }

    public static Process createOrderFulfillmentProcess(ProcessDefinition processDefinition) {
        var orderVariable = VariableTestData.createOrderVariable();
        var clientVariable = VariableTestData.createClientVariable();
        return Process.builder()
                .definition(processDefinition)
                .variables(List.of(orderVariable, clientVariable))
                .build();
    }

}
