package com.leorces.engine.event.activity.run;

import com.leorces.engine.event.activity.ActivityEvent;
import com.leorces.model.runtime.activity.ActivityExecution;

public class RunActivityEventAsync extends ActivityEvent {

    public final ActivityExecution activity;

    public RunActivityEventAsync(ActivityExecution activity) {
        super(activity);
        this.activity = activity;
    }

}
