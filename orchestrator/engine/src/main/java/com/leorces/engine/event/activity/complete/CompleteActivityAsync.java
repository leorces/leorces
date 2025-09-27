package com.leorces.engine.event.activity.complete;

import com.leorces.engine.event.activity.ActivityEvent;
import com.leorces.model.runtime.activity.ActivityExecution;

public class CompleteActivityAsync extends ActivityEvent {

    public ActivityExecution activity;

    public CompleteActivityAsync(ActivityExecution activity) {
        super(activity);
        this.activity = activity;
    }

}
