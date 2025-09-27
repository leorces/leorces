package com.leorces.engine.event.activity.run;

import com.leorces.engine.event.activity.ActivityEvent;
import com.leorces.model.definition.activity.ActivityDefinition;
import com.leorces.model.runtime.process.Process;

import java.util.List;

public class RunActivitiesEventAsync extends ActivityEvent {

    public final List<ActivityDefinition> definitions;
    public final Process process;

    public RunActivitiesEventAsync(List<ActivityDefinition> definitions, Process process) {
        super(process);
        this.definitions = definitions;
        this.process = process;
    }

}
