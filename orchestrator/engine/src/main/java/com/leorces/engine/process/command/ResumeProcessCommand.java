package com.leorces.engine.process.command;

import com.leorces.engine.core.ExecutionCommand;
import lombok.Builder;

@Builder
public record ResumeProcessCommand(
        String processId,
        String definitionId,
        String definitionKey
) implements ExecutionCommand {

    public static ResumeProcessCommand ofProcessId(String processId) {
        return ResumeProcessCommand.builder().processId(processId).build();
    }

    public static ResumeProcessCommand ofDefinitionId(String definitionId) {
        return ResumeProcessCommand.builder().definitionId(definitionId).build();
    }

    public static ResumeProcessCommand ofDefinitionKey(String definitionKey) {
        return ResumeProcessCommand.builder().definitionKey(definitionKey).build();
    }

    public boolean isIdentifierPresent() {
        return processId != null || definitionId != null || definitionKey != null;
    }

}
