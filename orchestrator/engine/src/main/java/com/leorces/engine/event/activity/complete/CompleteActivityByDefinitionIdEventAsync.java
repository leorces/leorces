package com.leorces.engine.event.activity.complete;

import com.leorces.engine.event.activity.ActivityEvent;

public class CompleteActivityByDefinitionIdEventAsync extends ActivityEvent {

    public final String definitionId;
    public final String processId;

    public CompleteActivityByDefinitionIdEventAsync(String definitionId, String processId) {
        super(processId);
        this.definitionId = definitionId;
        this.processId = processId;
    }

}
