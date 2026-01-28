package com.leorces.engine.activity.command;

import com.leorces.model.definition.activity.ActivityDefinition;
import com.leorces.model.runtime.process.Process;
import lombok.Builder;

import java.util.Optional;

@Builder
public record ExecutionResultCommand(
        String code,
        String scope,
        Process process,
        ExecutionResultType type
) implements com.leorces.engine.core.ExecutionResultCommand<Optional<ActivityDefinition>> {

    public static ExecutionResultCommand error(String code, String scope, Process process) {
        return ExecutionResultCommand.builder()
                .code(code)
                .scope(scope)
                .process(process)
                .type(ExecutionResultType.ERROR)
                .build();
    }

    public static ExecutionResultCommand escalation(String code, String scope, Process process) {
        return ExecutionResultCommand.builder()
                .code(code)
                .scope(scope)
                .process(process)
                .type(ExecutionResultType.ESCALATION)
                .build();
    }

    public enum ExecutionResultType {
        ERROR, ESCALATION
    }

}
