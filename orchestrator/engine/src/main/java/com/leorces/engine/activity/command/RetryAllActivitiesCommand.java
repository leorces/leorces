package com.leorces.engine.activity.command;

import com.leorces.engine.core.ExecutionCommand;
import com.leorces.model.runtime.activity.ActivityExecution;
import lombok.Builder;

import java.util.List;

@Builder(toBuilder = true)
public record RetryAllActivitiesCommand(
        String processId,
        List<ActivityExecution> activities
) implements ExecutionCommand {

    public static RetryAllActivitiesCommand of(List<ActivityExecution> activities) {
        return RetryAllActivitiesCommand.builder()
                .activities(activities)
                .build();
    }

    public static RetryAllActivitiesCommand of(String processId) {
        return RetryAllActivitiesCommand.builder()
                .processId(processId)
                .build();
    }

}
