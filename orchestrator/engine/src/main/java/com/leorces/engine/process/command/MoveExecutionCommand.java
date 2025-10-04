package com.leorces.engine.process.command;

import com.leorces.engine.core.ExecutionCommand;

public record MoveExecutionCommand(
        String processId,
        String activityId,
        String targetDefinitionId
) implements ExecutionCommand {

    public static MoveExecutionCommand of(String processId, String activityId, String targetDefinitionId) {
        return new MoveExecutionCommand(processId, activityId, targetDefinitionId);
    }

}
