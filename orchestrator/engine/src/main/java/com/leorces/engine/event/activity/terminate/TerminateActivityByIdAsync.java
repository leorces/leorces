package com.leorces.engine.event.activity.terminate;

import com.leorces.engine.event.activity.ActivityEvent;

public class TerminateActivityByIdAsync extends ActivityEvent {

    public final String activityId;

    public TerminateActivityByIdAsync(String activityId) {
        super(activityId);
        this.activityId = activityId;
    }

}
