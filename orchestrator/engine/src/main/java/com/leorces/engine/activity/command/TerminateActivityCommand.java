package com.leorces.engine.activity.command;

import com.leorces.engine.core.ExecutionCommand;
import com.leorces.model.runtime.activity.ActivityExecution;
import lombok.Builder;

@Builder
public record TerminateActivityCommand(
        String activityId,
        ActivityExecution activity,
        boolean withInterruption
) implements ExecutionCommand {

    public static TerminateActivityCommand of(String activityId) {
        return TerminateActivityCommand.builder()
                .activityId(activityId)
                .withInterruption(false)
                .build();
    }

    public static TerminateActivityCommand of(ActivityExecution activity, boolean withInterruption) {
        return TerminateActivityCommand.builder()
                .activity(activity)
                .withInterruption(withInterruption)
                .build();
    }

    public static TerminateActivityCommand of(String activityId, boolean withInterruption) {
        return TerminateActivityCommand.builder()
                .activityId(activityId)
                .withInterruption(withInterruption)
                .build();
    }

}
