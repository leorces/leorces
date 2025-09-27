package com.leorces.engine.event.activity.cancel;

import com.leorces.engine.event.activity.ActivityEvent;
import com.leorces.model.runtime.activity.ActivityExecution;

import java.util.List;

public class CancelActivitiesEvent extends ActivityEvent {

    public final List<ActivityExecution> activities;

    public CancelActivitiesEvent(List<ActivityExecution> activities) {
        super(activities);
        this.activities = activities;
    }

}
