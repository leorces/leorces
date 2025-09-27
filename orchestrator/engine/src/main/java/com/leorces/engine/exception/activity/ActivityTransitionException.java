package com.leorces.engine.exception.activity;

import com.leorces.model.runtime.activity.ActivityExecution;
import com.leorces.model.runtime.activity.ActivityState;

public class ActivityTransitionException extends RuntimeException {

    public ActivityTransitionException(String message) {
        super(message);
    }

    public static ActivityTransitionException create(ActivityExecution activity, ActivityState toState) {
        return new ActivityTransitionException(
                "Activity with id %s and definition id %s cannot transition from state: %s to state: %s in process: %s"
                        .formatted(activity.id(), activity.definitionId(), activity.state(), toState, activity.processId())
        );
    }

}
