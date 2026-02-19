package com.leorces.engine.admin.suspend.command;

import com.leorces.engine.core.ExecutionCommand;

import java.util.Map;

public record ResumeProcessDefinitionCommand(
        Map<String, Object> input
) implements ExecutionCommand {

    public static ResumeProcessDefinitionCommand of(Map<String, Object> input) {
        return new ResumeProcessDefinitionCommand(input);
    }

}
