package com.leorces.engine.activity.command;

import com.leorces.engine.core.ExecutionCommand;
import com.leorces.model.definition.activity.ActivityDefinition;
import com.leorces.model.runtime.activity.ActivityExecution;
import com.leorces.model.runtime.process.Process;
import lombok.Builder;

@Builder
public record RunActivityCommand(
        Process process,
        ActivityDefinition definition,
        String processId,
        String definitionId,
        ActivityExecution activity
) implements ExecutionCommand {

    public static RunActivityCommand of(Process process, ActivityDefinition definition) {
        return RunActivityCommand.builder()
                .process(process)
                .definition(definition)
                .build();
    }

    public static RunActivityCommand of(ActivityExecution activity) {
        return RunActivityCommand.builder()
                .activity(activity)
                .build();
    }

    public static RunActivityCommand of(String definitionId, String processId) {
        return RunActivityCommand.builder()
                .definitionId(definitionId)
                .processId(processId)
                .build();
    }

}
