package com.leorces.engine.event.activity.retry;

import com.leorces.engine.event.activity.ActivityEvent;
import com.leorces.model.runtime.activity.ActivityExecution;

import java.util.List;

public class RetryActivitiesEventAsync extends ActivityEvent {

    public final List<ActivityExecution> activities;

    public RetryActivitiesEventAsync(List<ActivityExecution> activities) {
        super(activities);
        this.activities = activities;
    }

}
