package com.leorces.engine.activity.command;

import com.leorces.engine.core.ExecutionCommand;
import com.leorces.model.definition.activity.ActivityDefinition;
import com.leorces.model.runtime.process.Process;

import java.util.List;

public record RunAllActivitiesCommand(
        Process process,
        List<ActivityDefinition> nextActivities
) implements ExecutionCommand {

    public static RunAllActivitiesCommand of(Process process, List<ActivityDefinition> nextActivities) {
        return new RunAllActivitiesCommand(process, nextActivities);
    }

}
