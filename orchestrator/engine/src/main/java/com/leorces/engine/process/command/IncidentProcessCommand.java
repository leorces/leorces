package com.leorces.engine.process.command;

import com.leorces.engine.core.ExecutionCommand;

public record IncidentProcessCommand(
        String processId
) implements ExecutionCommand {

    public static IncidentProcessCommand of(String processId) {
        return new IncidentProcessCommand(processId);
    }

}