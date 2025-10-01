package com.leorces.engine.activity.command;

import com.leorces.engine.core.ExecutionCommand;
import com.leorces.model.runtime.activity.ActivityExecution;
import lombok.Builder;

import java.util.Map;

@Builder
public record CompleteActivityCommand(
        String activityId,
        ActivityExecution activity,
        Map<String, Object> variables
) implements ExecutionCommand {

    public static CompleteActivityCommand of(ActivityExecution activity) {
        return CompleteActivityCommand.builder()
                .activity(activity)
                .variables(Map.of())
                .build();
    }

    public static CompleteActivityCommand of(ActivityExecution activity, Map<String, Object> variables) {
        return CompleteActivityCommand.builder()
                .activity(activity)
                .variables(variables)
                .build();
    }

    public static CompleteActivityCommand of(String activityId) {
        return CompleteActivityCommand.builder()
                .activityId(activityId)
                .variables(Map.of())
                .build();
    }

    public static CompleteActivityCommand of(String activityId, Map<String, Object> variables) {
        return CompleteActivityCommand.builder()
                .activityId(activityId)
                .variables(variables)
                .build();
    }

}