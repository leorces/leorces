package com.leorces.engine.process.command;

import com.leorces.engine.core.ExecutionCommand;

public record TerminateProcessCommand(
        String processId
) implements ExecutionCommand {

    public static TerminateProcessCommand of(String processId) {
        return new TerminateProcessCommand(processId);
    }

}
