package com.leorces.engine.process.command;

import com.leorces.engine.core.ExecutionResultCommand;
import com.leorces.model.runtime.process.Process;
import com.leorces.model.search.ProcessFilter;

public record FindProcessByFilterCommand(
        ProcessFilter filter
) implements ExecutionResultCommand<Process> {
}
