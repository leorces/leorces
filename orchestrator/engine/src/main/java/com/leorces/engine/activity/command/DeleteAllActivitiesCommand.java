package com.leorces.engine.activity.command;

import com.leorces.engine.core.ExecutionCommand;
import com.leorces.model.runtime.activity.ActivityExecution;

import java.util.List;

public record DeleteAllActivitiesCommand(
        List<ActivityExecution> activities
) implements ExecutionCommand {

    public static DeleteAllActivitiesCommand of(List<ActivityExecution> activities) {
        return new DeleteAllActivitiesCommand(activities);
    }

}
