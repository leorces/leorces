package com.leorces.engine.process.command;

import com.leorces.engine.core.ExecutionCommand;
import lombok.Builder;

@Builder
public record SuspendProcessCommand(
        String processId
) implements ExecutionCommand {

    public static SuspendProcessCommand of(String processId) {
        return SuspendProcessCommand.builder().processId(processId).build();
    }

}
