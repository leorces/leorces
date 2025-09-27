package com.leorces.engine.event.process.start;


import com.leorces.engine.event.process.ProcessEvent;
import com.leorces.model.runtime.activity.ActivityExecution;


public class StartProcessByCallActivityEvent extends ProcessEvent {

    public final ActivityExecution activity;

    public StartProcessByCallActivityEvent(ActivityExecution activity) {
        super(activity.process());
        this.activity = activity;
    }

}
