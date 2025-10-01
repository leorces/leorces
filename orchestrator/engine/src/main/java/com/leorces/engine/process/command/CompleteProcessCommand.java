package com.leorces.engine.process.command;

import com.leorces.engine.core.ExecutionCommand;

public record CompleteProcessCommand(
        String processId
) implements ExecutionCommand {

    public static CompleteProcessCommand of(String processId) {
        return new CompleteProcessCommand(processId);
    }

}
