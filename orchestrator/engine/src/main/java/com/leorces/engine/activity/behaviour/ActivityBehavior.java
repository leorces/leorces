package com.leorces.engine.activity.behaviour;

import com.leorces.model.definition.activity.ActivityDefinition;
import com.leorces.model.definition.activity.ActivityType;
import com.leorces.model.runtime.activity.ActivityExecution;

import java.util.List;

public interface ActivityBehavior {

    void run(ActivityExecution activity);

    ActivityCompletionResult complete(ActivityExecution activity);

    ActivityCompletionResult terminate(ActivityExecution activity);

    boolean fail(ActivityExecution activity);

    void retry(ActivityExecution activity);

    List<ActivityDefinition> getNextActivities(ActivityExecution activity);

    ActivityType type();

}
