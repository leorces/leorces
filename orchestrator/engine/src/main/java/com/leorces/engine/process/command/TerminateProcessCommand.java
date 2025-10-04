package com.leorces.engine.process.command;

import com.leorces.engine.core.ExecutionCommand;

public record TerminateProcessCommand(
        String processId,
        boolean terminateCallActivity
) implements ExecutionCommand {

    public static TerminateProcessCommand of(String processId) {
        return new TerminateProcessCommand(processId, true);
    }

    public static TerminateProcessCommand of(String processId, boolean terminateCallActivity) {
        return new TerminateProcessCommand(processId, terminateCallActivity);
    }

}
