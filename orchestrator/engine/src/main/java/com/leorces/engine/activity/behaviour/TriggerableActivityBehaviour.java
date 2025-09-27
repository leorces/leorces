package com.leorces.engine.activity.behaviour;

import com.leorces.model.definition.activity.ActivityDefinition;
import com.leorces.model.runtime.process.Process;

public interface TriggerableActivityBehaviour extends ActivityBehavior {

    void trigger(Process process, ActivityDefinition definition);

}
