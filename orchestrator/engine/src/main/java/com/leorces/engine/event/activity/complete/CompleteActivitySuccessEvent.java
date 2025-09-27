package com.leorces.engine.event.activity.complete;

import com.leorces.engine.event.activity.ActivityEvent;
import com.leorces.model.runtime.activity.ActivityExecution;

public class CompleteActivitySuccessEvent extends ActivityEvent {

    public final ActivityExecution activity;

    public CompleteActivitySuccessEvent(ActivityExecution activity) {
        super(activity);
        this.activity = activity;
    }

}
