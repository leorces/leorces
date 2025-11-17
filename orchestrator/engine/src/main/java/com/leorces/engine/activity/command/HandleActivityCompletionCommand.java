package com.leorces.engine.activity.command;

import com.leorces.engine.core.ExecutionCommand;
import com.leorces.model.runtime.activity.ActivityExecution;

public record HandleActivityCompletionCommand(
        ActivityExecution activity
) implements ExecutionCommand {

    public static HandleActivityCompletionCommand of(ActivityExecution activity) {
        return new HandleActivityCompletionCommand(activity);
    }

}
