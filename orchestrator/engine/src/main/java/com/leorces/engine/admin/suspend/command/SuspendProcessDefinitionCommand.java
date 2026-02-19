package com.leorces.engine.admin.suspend.command;

import com.leorces.engine.core.ExecutionCommand;

import java.util.Map;

public record SuspendProcessDefinitionCommand(
        Map<String, Object> input
) implements ExecutionCommand {

    public static SuspendProcessDefinitionCommand of(Map<String, Object> input) {
        return new SuspendProcessDefinitionCommand(input);
    }

}
