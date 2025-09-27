package com.leorces.engine.event.activity.terminate;

import com.leorces.engine.event.activity.ActivityEvent;

public class TerminateActivitiesByProcessIdEvent extends ActivityEvent {

    public final String processId;

    public TerminateActivitiesByProcessIdEvent(String processId) {
        super(processId);
        this.processId = processId;
    }

}
