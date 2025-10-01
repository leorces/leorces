package com.leorces.engine.correlation.command;

import com.leorces.engine.core.ExecutionCommand;
import com.leorces.model.runtime.activity.ActivityExecution;

public record CorrelateErrorCommand(
        ActivityExecution activity
) implements ExecutionCommand {

    public static CorrelateErrorCommand of(ActivityExecution activity) {
        return new CorrelateErrorCommand(activity);
    }

}
