package com.leorces.engine.activity.behaviour;

import com.leorces.model.definition.activity.ActivityDefinition;
import com.leorces.model.runtime.activity.ActivityExecution;
import lombok.Builder;

import java.util.List;

@Builder
public record ActivityCompletionResult(
        ActivityExecution activity,
        boolean isCompleted,
        List<ActivityDefinition> nextActivities
) {

    public static ActivityCompletionResult completed(ActivityExecution activity, List<ActivityDefinition> nextActivities) {
        return ActivityCompletionResult.builder()
                .activity(activity)
                .nextActivities(nextActivities)
                .isCompleted(true)
                .build();
    }

    public static ActivityCompletionResult incompleted(ActivityExecution activity, List<ActivityDefinition> nextActivities) {
        return ActivityCompletionResult.builder()
                .activity(activity)
                .nextActivities(nextActivities)
                .isCompleted(false)
                .build();
    }

}
