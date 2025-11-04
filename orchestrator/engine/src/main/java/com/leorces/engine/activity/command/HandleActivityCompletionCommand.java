package com.leorces.engine.activity.command;

import com.leorces.engine.activity.behaviour.ActivityCompletionResult;
import com.leorces.engine.core.ExecutionCommand;

public record HandleActivityCompletionCommand(
        ActivityCompletionResult result
) implements ExecutionCommand {

    public static HandleActivityCompletionCommand of(ActivityCompletionResult result) {
        return new HandleActivityCompletionCommand(result);
    }

}
