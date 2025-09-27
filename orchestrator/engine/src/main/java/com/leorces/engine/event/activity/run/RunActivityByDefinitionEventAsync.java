package com.leorces.engine.event.activity.run;

import com.leorces.engine.event.activity.ActivityEvent;
import com.leorces.model.definition.activity.ActivityDefinition;
import com.leorces.model.runtime.process.Process;

public class RunActivityByDefinitionEventAsync extends ActivityEvent {

    public final ActivityDefinition definition;
    public final Process process;

    public RunActivityByDefinitionEventAsync(ActivityDefinition definition, Process process) {
        super(process);
        this.definition = definition;
        this.process = process;
    }

}
