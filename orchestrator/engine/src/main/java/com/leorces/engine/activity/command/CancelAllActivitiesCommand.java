package com.leorces.engine.activity.command;

import com.leorces.engine.core.ExecutionCommand;
import com.leorces.model.runtime.activity.ActivityExecution;

import java.util.List;

public record CancelAllActivitiesCommand(
        List<ActivityExecution> activities
) implements ExecutionCommand {

    public static CancelAllActivitiesCommand of(List<ActivityExecution> activities) {
        return new CancelAllActivitiesCommand(activities);
    }

}
