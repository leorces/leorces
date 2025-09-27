package com.leorces.engine.activity.behaviour;

import com.leorces.model.definition.activity.ActivityType;
import com.leorces.model.runtime.activity.ActivityExecution;

public interface ActivityBehavior {

    void run(ActivityExecution activity);

    ActivityExecution complete(ActivityExecution activity);

    ActivityType type();

}
