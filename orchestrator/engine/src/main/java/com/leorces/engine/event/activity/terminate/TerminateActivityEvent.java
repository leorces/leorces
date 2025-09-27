package com.leorces.engine.event.activity.terminate;

import com.leorces.engine.event.activity.ActivityEvent;
import com.leorces.model.runtime.activity.ActivityExecution;

public class TerminateActivityEvent extends ActivityEvent {

    public final ActivityExecution activity;

    public TerminateActivityEvent(ActivityExecution activity) {
        super(activity);
        this.activity = activity;
    }

}
