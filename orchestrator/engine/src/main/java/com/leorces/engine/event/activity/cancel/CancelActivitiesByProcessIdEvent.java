package com.leorces.engine.event.activity.cancel;

import com.leorces.engine.event.activity.ActivityEvent;

public class CancelActivitiesByProcessIdEvent extends ActivityEvent {

    public final String processId;

    public CancelActivitiesByProcessIdEvent(String processId) {
        super(processId);
        this.processId = processId;
    }

}