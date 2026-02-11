package com.leorces.engine.activity.command;

import com.leorces.engine.core.ExecutionCommand;
import com.leorces.model.runtime.activity.ActivityExecution;
import lombok.Builder;

@Builder
public record DeleteActivityCommand(
        String activityId,
        ActivityExecution activity
) implements ExecutionCommand {

    public static DeleteActivityCommand of(String activityId) {
        return DeleteActivityCommand.builder().activityId(activityId).build();
    }

    public static DeleteActivityCommand of(ActivityExecution activity) {
        return DeleteActivityCommand.builder().activity(activity).build();
    }

}
