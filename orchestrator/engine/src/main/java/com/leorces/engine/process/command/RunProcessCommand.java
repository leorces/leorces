package com.leorces.engine.process.command;

import com.leorces.engine.core.ExecutionResultCommand;
import com.leorces.model.runtime.activity.ActivityExecution;
import com.leorces.model.runtime.process.Process;
import lombok.Builder;

import java.util.Map;

@Builder
public record RunProcessCommand(
        Process process,
        ActivityExecution callActivity,
        String definitionId,
        String definitionKey,
        String businessKey,
        Map<String, Object> variables
) implements ExecutionResultCommand<Process> {

    public static RunProcessCommand of(Process process) {
        return RunProcessCommand.builder()
                .process(process)
                .build();
    }

    public static RunProcessCommand byCallActivity(ActivityExecution callActivity) {
        return RunProcessCommand.builder()
                .callActivity(callActivity)
                .build();
    }

    public static RunProcessCommand byDefinitionId(String definitionId,
                                                   String businessKey,
                                                   Map<String, Object> variables) {
        return RunProcessCommand.builder()
                .definitionId(definitionId)
                .businessKey(businessKey)
                .variables(variables)
                .build();
    }

    public static RunProcessCommand byDefinitionKey(String definitionKey,
                                                    String businessKey,
                                                    Map<String, Object> variables) {
        return RunProcessCommand.builder()
                .definitionKey(definitionKey)
                .businessKey(businessKey)
                .variables(variables)
                .build();
    }

}
