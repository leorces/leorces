package com.leorces.engine.event.activity.run;

import com.leorces.engine.event.activity.ActivityEvent;

public class RunActivityByDefinitionIdAsync extends ActivityEvent {

    public String definitionId;
    public String processId;

    public RunActivityByDefinitionIdAsync(String definitionId, String processId) {
        super(definitionId);
        this.definitionId = definitionId;
        this.processId = processId;
    }

}
