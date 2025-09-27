package com.leorces.engine.event.activity.retry;

import com.leorces.engine.event.activity.ActivityEvent;

public class RetryActivityByIdEventAsync extends ActivityEvent {

    public final String activityId;

    public RetryActivityByIdEventAsync(String activityId) {
        super(activityId);
        this.activityId = activityId;
    }

}
