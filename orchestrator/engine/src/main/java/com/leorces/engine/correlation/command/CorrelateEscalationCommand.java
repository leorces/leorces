package com.leorces.engine.correlation.command;

import com.leorces.engine.core.ExecutionCommand;
import com.leorces.model.runtime.activity.ActivityExecution;

public record CorrelateEscalationCommand(
        ActivityExecution escalationEvent
) implements ExecutionCommand {

    public static CorrelateEscalationCommand of(ActivityExecution escalationEvent) {
        return new CorrelateEscalationCommand(escalationEvent);
    }

}
