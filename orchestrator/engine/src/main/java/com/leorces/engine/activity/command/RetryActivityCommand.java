package com.leorces.engine.activity.command;

import com.leorces.engine.core.ExecutionCommand;
import com.leorces.model.runtime.activity.ActivityExecution;
import lombok.Builder;

@Builder
public record RetryActivityCommand(
        String activityId,
        ActivityExecution activity
) implements ExecutionCommand {

    public static RetryActivityCommand of(ActivityExecution activity) {
        return RetryActivityCommand.builder()
                .activity(activity)
                .build();
    }

    public static RetryActivityCommand of(String activityId) {
        return RetryActivityCommand.builder()
                .activityId(activityId)
                .build();
    }

}