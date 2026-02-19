package com.leorces.engine.process.command;

import com.leorces.engine.core.ExecutionCommand;
import lombok.Builder;

@Builder
public record ResumeProcessCommand(
        String processId
) implements ExecutionCommand {

    public static ResumeProcessCommand of(String processId) {
        return ResumeProcessCommand.builder().processId(processId).build();
    }

}
