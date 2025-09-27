package com.leorces.engine.event.activity.fail;

import com.leorces.engine.event.activity.ActivityEvent;

import java.util.Map;

public class FailActivityByIdEventAsync extends ActivityEvent {

    public final String activityId;
    public final Map<String, Object> variables;

    public FailActivityByIdEventAsync(String activityId, Map<String, Object> variables) {
        super(activityId);
        this.activityId = activityId;
        this.variables = variables;
    }

}
