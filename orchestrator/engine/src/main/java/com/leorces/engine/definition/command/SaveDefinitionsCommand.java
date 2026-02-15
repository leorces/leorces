package com.leorces.engine.definition.command;

import com.leorces.engine.core.ExecutionResultCommand;
import com.leorces.model.definition.ProcessDefinition;

import java.util.List;

public record SaveDefinitionsCommand(
        List<ProcessDefinition> definitions
) implements ExecutionResultCommand<List<ProcessDefinition>> {
}
