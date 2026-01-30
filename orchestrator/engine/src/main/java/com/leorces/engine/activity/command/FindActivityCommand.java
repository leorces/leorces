package com.leorces.engine.activity.command;

import com.leorces.engine.core.ExecutionResultCommand;
import com.leorces.model.runtime.activity.ActivityExecution;
import lombok.Builder;

@Builder
public record FindActivityCommand(
        String activityId,
        String processId,
        String definitionId
) implements ExecutionResultCommand<ActivityExecution> {

    public static FindActivityCommand of(String activityId,
                                         String processId,
                                         String definitionId) {
        return FindActivityCommand.builder()
                .activityId(activityId)
                .processId(processId)
                .definitionId(definitionId)
                .build();
    }

    public static FindActivityCommand byId(String activityId) {
        return FindActivityCommand.builder()
                .activityId(activityId)
                .build();
    }

    public static FindActivityCommand byDefinitionId(String processId, String definitionId) {
        return FindActivityCommand.builder()
                .processId(processId)
                .definitionId(definitionId)
                .build();
    }

}
