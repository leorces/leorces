package com.leorces.engine.process.command;

import com.leorces.engine.core.ExecutionCommand;
import com.leorces.model.runtime.process.Process;
import lombok.Builder;

@Builder(toBuilder = true)
public record CompleteProcessCommand(
        String processId,
        Process process
) implements ExecutionCommand {

    public static CompleteProcessCommand of(String processId) {
        return CompleteProcessCommand.builder().processId(processId).build();
    }

    public static CompleteProcessCommand of(Process process) {
        return CompleteProcessCommand.builder().process(process).build();
    }

}
