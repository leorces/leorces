package com.leorces.engine.activity.command;

import com.leorces.engine.core.ExecutionCommand;
import com.leorces.model.runtime.activity.ActivityExecution;

import java.util.List;

public record TerminateAllActivitiesCommand(
        List<ActivityExecution> activities
) implements ExecutionCommand {

    public static TerminateAllActivitiesCommand of(List<ActivityExecution> activities) {
        return new TerminateAllActivitiesCommand(activities);
    }

}
