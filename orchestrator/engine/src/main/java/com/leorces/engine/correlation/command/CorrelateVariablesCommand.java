package com.leorces.engine.correlation.command;

import com.leorces.engine.core.ExecutionCommand;
import com.leorces.model.runtime.process.Process;
import com.leorces.model.runtime.variable.Variable;

import java.util.List;

public record CorrelateVariablesCommand(
        Process process,
        List<Variable> variables
) implements ExecutionCommand {

    public static CorrelateVariablesCommand of(Process process, List<Variable> variables) {
        return new CorrelateVariablesCommand(process, variables);
    }

}
