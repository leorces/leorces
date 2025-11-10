package com.leorces.engine.variables.command;

import com.leorces.engine.core.ExecutionCommand;
import com.leorces.model.runtime.activity.ActivityExecution;

import java.util.Map;

public record SetActivityVariablesCommand(
        ActivityExecution activity,
        Map<String, Object> variables
) implements ExecutionCommand {

    public static SetActivityVariablesCommand of(ActivityExecution activity,
                                                 Map<String, Object> variables) {
        return new SetActivityVariablesCommand(activity, variables);
    }

}
