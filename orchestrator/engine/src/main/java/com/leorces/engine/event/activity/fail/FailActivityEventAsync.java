package com.leorces.engine.event.activity.fail;

import com.leorces.engine.event.activity.ActivityEvent;
import com.leorces.model.runtime.activity.ActivityExecution;

public class FailActivityEventAsync extends ActivityEvent {

    public ActivityExecution activity;

    public FailActivityEventAsync(ActivityExecution activity) {
        super(activity);
        this.activity = activity;
    }

}
