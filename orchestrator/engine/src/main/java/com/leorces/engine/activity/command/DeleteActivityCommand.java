package com.leorces.engine.activity.command;

import com.leorces.engine.core.ExecutionCommand;

public record DeleteActivityCommand(
        String activityId
) implements ExecutionCommand {

    public static DeleteActivityCommand of(String activityId) {
        return new DeleteActivityCommand(activityId);
    }

}
