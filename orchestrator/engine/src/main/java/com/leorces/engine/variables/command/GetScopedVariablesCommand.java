package com.leorces.engine.variables.command;

import com.leorces.engine.core.ExecutionResultCommand;
import com.leorces.model.runtime.activity.ActivityExecution;

import java.util.Map;

public record GetScopedVariablesCommand(
        ActivityExecution activity
) implements ExecutionResultCommand<Map<String, Object>> {

    public static GetScopedVariablesCommand of(ActivityExecution activity) {
        return new GetScopedVariablesCommand(activity);
    }

}
