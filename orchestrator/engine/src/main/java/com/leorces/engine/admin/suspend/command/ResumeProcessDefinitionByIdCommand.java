package com.leorces.engine.admin.suspend.command;

import com.leorces.engine.admin.common.command.JobCommand;

import java.util.Map;

public record ResumeProcessDefinitionByIdCommand(
        String processDefinitionKey,
        int processDefinitionVersion,
        Map<String, Object> input
) implements JobCommand {
}
