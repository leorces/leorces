package com.leorces.engine.activity.command;

import com.leorces.engine.core.ExecutionCommand;
import com.leorces.model.definition.activity.ActivityDefinition;
import com.leorces.model.runtime.process.Process;

import java.util.List;

public record HandleActivityCompletionWithNextActivitiesCommand(
        Process process,
        List<ActivityDefinition> nextActivities
) implements ExecutionCommand {

    public static HandleActivityCompletionWithNextActivitiesCommand of(Process process, List<ActivityDefinition> nextActivities) {
        return new HandleActivityCompletionWithNextActivitiesCommand(process, nextActivities);
    }

}
