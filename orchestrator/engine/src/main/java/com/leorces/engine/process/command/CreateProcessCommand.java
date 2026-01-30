package com.leorces.engine.process.command;

import com.leorces.engine.core.ExecutionResultCommand;
import com.leorces.model.runtime.activity.ActivityExecution;
import com.leorces.model.runtime.process.Process;
import lombok.Builder;

import java.util.Map;

@Builder
public record CreateProcessCommand(
        ActivityExecution callActivity,
        String definitionId,
        String definitionKey,
        String businessKey,
        Map<String, Object> variables
) implements ExecutionResultCommand<Process> {

    public static CreateProcessCommand of(ActivityExecution callActivity,
                                          String definitionId,
                                          String definitionKey,
                                          String businessKey,
                                          Map<String, Object> variables) {
        return CreateProcessCommand.builder()
                .callActivity(callActivity)
                .definitionId(definitionId)
                .definitionKey(definitionKey)
                .businessKey(businessKey)
                .variables(variables)
                .build();
    }

    public static CreateProcessCommand byCallActivity(ActivityExecution callActivity) {
        return CreateProcessCommand.builder()
                .callActivity(callActivity)
                .build();
    }

    public static CreateProcessCommand byDefinitionId(String definitionId,
                                                      String businessKey,
                                                      Map<String, Object> variables) {
        return CreateProcessCommand.builder()
                .definitionId(definitionId)
                .businessKey(businessKey)
                .variables(variables)
                .build();
    }

    public static CreateProcessCommand byDefinitionKey(String definitionKey,
                                                       String businessKey,
                                                       Map<String, Object> variables) {
        return CreateProcessCommand.builder()
                .definitionKey(definitionKey)
                .businessKey(businessKey)
                .variables(variables)
                .build();
    }

}
