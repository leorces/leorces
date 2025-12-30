package com.leorces.engine.process.command;

import com.leorces.engine.core.ExecutionCommand;
import lombok.Builder;

@Builder
public record SuspendProcessCommand(
        String processId,
        String definitionId,
        String definitionKey
) implements ExecutionCommand {

    public static SuspendProcessCommand ofProcessId(String processId) {
        return SuspendProcessCommand.builder().processId(processId).build();
    }

    public static SuspendProcessCommand ofDefinitionId(String definitionId) {
        return SuspendProcessCommand.builder().definitionId(definitionId).build();
    }

    public static SuspendProcessCommand ofDefinitionKey(String definitionKey) {
        return SuspendProcessCommand.builder().definitionKey(definitionKey).build();
    }

    public boolean isIdentifierPresent() {
        return processId != null || definitionId != null || definitionKey != null;
    }

}
