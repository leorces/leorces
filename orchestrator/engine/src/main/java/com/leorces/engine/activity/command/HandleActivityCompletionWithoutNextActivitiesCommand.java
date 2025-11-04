package com.leorces.engine.activity.command;

import com.leorces.engine.core.ExecutionCommand;
import com.leorces.model.runtime.activity.ActivityExecution;

public record HandleActivityCompletionWithoutNextActivitiesCommand(
        ActivityExecution activity
) implements ExecutionCommand {

    public static HandleActivityCompletionWithoutNextActivitiesCommand of(ActivityExecution activity) {
        return new HandleActivityCompletionWithoutNextActivitiesCommand(activity);
    }

}
