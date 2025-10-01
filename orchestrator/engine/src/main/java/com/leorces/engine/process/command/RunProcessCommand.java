package com.leorces.engine.process.command;

import com.leorces.engine.core.ExecutionCommand;
import com.leorces.model.runtime.activity.ActivityExecution;

public record RunProcessCommand(
        ActivityExecution callActivity
) implements ExecutionCommand {

    public static RunProcessCommand of(ActivityExecution callActivity) {
        return new RunProcessCommand(callActivity);
    }

}
