package com.leorces.engine.activity.command;

import com.leorces.engine.core.ExecutionResultCommand;
import com.leorces.model.definition.activity.ActivityDefinition;
import com.leorces.model.runtime.process.Process;
import lombok.Builder;

import java.util.Optional;

@Builder
public record FindActivityHandlerCommand(
        String code,
        String scope,
        Process process,
        ExecutionResultType type
) implements ExecutionResultCommand<Optional<ActivityDefinition>> {

    public static FindActivityHandlerCommand error(String code, String scope, Process process) {
        return FindActivityHandlerCommand.builder()
                .code(code)
                .scope(scope)
                .process(process)
                .type(ExecutionResultType.ERROR)
                .build();
    }

    public static FindActivityHandlerCommand escalation(String code, String scope, Process process) {
        return FindActivityHandlerCommand.builder()
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
