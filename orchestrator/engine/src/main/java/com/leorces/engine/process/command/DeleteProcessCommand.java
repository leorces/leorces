package com.leorces.engine.process.command;

import com.leorces.engine.core.ExecutionCommand;

public record DeleteProcessCommand(
        String processId,
        boolean deleteCallActivity
) implements ExecutionCommand {

    public static DeleteProcessCommand of(String processId) {
        return new DeleteProcessCommand(processId, true);
    }

    public static DeleteProcessCommand of(String processId, boolean deleteCallActivity) {
        return new DeleteProcessCommand(processId, deleteCallActivity);
    }

}