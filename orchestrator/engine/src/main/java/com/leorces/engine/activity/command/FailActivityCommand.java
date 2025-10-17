package com.leorces.engine.activity.command;

import com.leorces.engine.core.ExecutionCommand;
import com.leorces.model.runtime.activity.ActivityExecution;
import com.leorces.model.runtime.activity.ActivityFailure;
import lombok.Builder;

import java.util.Map;

@Builder
public record FailActivityCommand(
        String activityId,
        ActivityExecution activity,
        ActivityFailure failure,
        Map<String, Object> variables
) implements ExecutionCommand {

    public static FailActivityCommand of(String activityId,
                                         ActivityFailure failure,
                                         Map<String, Object> variables) {
        return FailActivityCommand.builder()
                .activityId(activityId)
                .failure(failure)
                .variables(variables)
                .build();
    }

    public static FailActivityCommand of(String activityId, ActivityFailure failure) {
        return FailActivityCommand.builder()
                .activityId(activityId)
                .failure(failure)
                .variables(Map.of())
                .build();
    }

    public static FailActivityCommand of(String activityId) {
        return FailActivityCommand.builder()
                .activityId(activityId)
                .variables(Map.of())
                .build();
    }

    public static FailActivityCommand of(ActivityExecution activity) {
        return FailActivityCommand.builder()
                .activity(activity)
                .variables(Map.of())
                .build();
    }

    public static FailActivityCommand of(ActivityExecution activity, ActivityFailure failure) {
        return FailActivityCommand.builder()
                .activity(activity)
                .failure(failure)
                .variables(Map.of())
                .build();
    }

}