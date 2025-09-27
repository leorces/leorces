package com.leorces.engine.event.activity.complete;

import com.leorces.engine.event.activity.ActivityEvent;

import java.util.Map;

public class CompleteActivityByIdEventAsync extends ActivityEvent {

    public final String activityId;
    public final Map<String, Object> variables;

    public CompleteActivityByIdEventAsync(String activityId, Map<String, Object> variables) {
        super(activityId);
        this.activityId = activityId;
        this.variables = variables;
    }

}
