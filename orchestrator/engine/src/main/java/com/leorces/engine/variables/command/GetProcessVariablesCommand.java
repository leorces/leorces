package com.leorces.engine.variables.command;

import com.leorces.engine.core.ExecutionResultCommand;

import java.util.Map;

public record GetProcessVariablesCommand(
        String processId
) implements ExecutionResultCommand<Map<String, Object>> {

    public static GetProcessVariablesCommand of(String processId) {
        return new GetProcessVariablesCommand(processId);
    }

}
