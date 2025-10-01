package com.leorces.engine.activity.command;

import com.leorces.engine.core.ExecutionCommand;
import com.leorces.model.definition.activity.ActivityDefinition;
import com.leorces.model.runtime.process.Process;

public record TriggerActivityCommand(
        Process process,
        ActivityDefinition definition
) implements ExecutionCommand {

    public static TriggerActivityCommand of(Process process, ActivityDefinition definition) {
        return new TriggerActivityCommand(process, definition);
    }

}
