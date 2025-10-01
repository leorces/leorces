package com.leorces.engine.variables.command;

import com.leorces.engine.core.ExecutionCommand;
import com.leorces.model.runtime.process.Process;
import lombok.Builder;

import java.util.Map;

@Builder
public record SetVariablesCommand(
        String executionId,
        Process process,
        Map<String, Object> variables,
        boolean local
) implements ExecutionCommand {

    public static SetVariablesCommand of(Process process,
                                         Map<String, Object> variables) {
        return SetVariablesCommand.builder()
                .process(process)
                .variables(variables)
                .local(true)
                .build();
    }

    public static SetVariablesCommand of(String executionId,
                                         Map<String, Object> variables,
                                         boolean local) {
        return SetVariablesCommand.builder()
                .executionId(executionId)
                .variables(variables)
                .local(local)
                .build();
    }

}
