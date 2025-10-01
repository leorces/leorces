package com.leorces.engine.activity.behaviour;

import com.leorces.model.definition.activity.ActivityDefinition;
import com.leorces.model.definition.activity.ActivityType;
import com.leorces.model.runtime.process.Process;

public interface TriggerableActivityBehaviour {

    void trigger(Process process, ActivityDefinition definition);

    ActivityType type();

}
