package com.leorces.engine.activity.command;

import com.leorces.engine.core.ExecutionResultCommand;
import com.leorces.model.definition.activity.ActivityDefinition;
import com.leorces.model.runtime.activity.ActivityExecution;
import com.leorces.model.runtime.process.Process;
import lombok.Builder;

@Builder
public record CreateActivityCommand(
        ActivityDefinition definition,
        Process process,
        String definitionId,
        String processId
) implements ExecutionResultCommand<ActivityExecution> {

    public static CreateActivityCommand of(ActivityDefinition definition,
                                           Process process,
                                           String definitionId,
                                           String processId) {
        return CreateActivityCommand.builder()
                .definition(definition)
                .process(process)
                .definitionId(definitionId)
                .processId(processId)
                .build();
    }

    public static CreateActivityCommand of(ActivityDefinition definition, Process process) {
        return CreateActivityCommand.builder()
                .definition(definition)
                .process(process)
                .build();
    }

    public static CreateActivityCommand of(String definitionId, String processId) {
        return CreateActivityCommand.builder()
                .definitionId(definitionId)
                .processId(processId)
                .build();
    }

}
