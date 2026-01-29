package com.leorces.engine.variables.command;

import com.leorces.engine.core.ExecutionResultCommand;
import com.leorces.model.runtime.activity.ActivityExecution;
import com.leorces.model.runtime.variable.Variable;

import java.util.List;
import java.util.Map;

public record EvaluateVariablesCommand(
        ActivityExecution activity,
        Map<String, Object> variables
) implements ExecutionResultCommand<List<Variable>> {

    public static EvaluateVariablesCommand of(ActivityExecution activity, Map<String, Object> variables) {
        return new EvaluateVariablesCommand(activity, variables);
    }

}
