package com.leorces.engine.process.command;

import com.leorces.engine.core.ExecutionCommand;

public record CancelProcessCommand(
        String processId
) implements ExecutionCommand {

    public static CancelProcessCommand of(String processId) {
        return new CancelProcessCommand(processId);
    }

}
