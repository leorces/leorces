package com.leorces.engine.process.command;

import com.leorces.engine.core.ExecutionCommand;

public record ResolveProcessIncidentCommand(
        String processId
) implements ExecutionCommand {

    public static ResolveProcessIncidentCommand of(String processId) {
        return new ResolveProcessIncidentCommand(processId);
    }

}