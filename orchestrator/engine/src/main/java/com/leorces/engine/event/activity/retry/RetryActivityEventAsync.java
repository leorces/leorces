package com.leorces.engine.event.activity.retry;

import com.leorces.engine.event.activity.ActivityEvent;
import com.leorces.model.runtime.activity.ActivityExecution;

public class RetryActivityEventAsync extends ActivityEvent {

    public final ActivityExecution activity;

    public RetryActivityEventAsync(ActivityExecution activity) {
        super(activity);
        this.activity = activity;
    }

}
