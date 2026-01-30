package com.leorces.engine.process.command;

import com.leorces.engine.core.ExecutionResultCommand;
import com.leorces.model.runtime.activity.ActivityExecution;
import com.leorces.model.runtime.process.Process;

public record CreateProcessByCallActivityCommand(
        ActivityExecution callActivity
) implements ExecutionResultCommand<Process> {
}
