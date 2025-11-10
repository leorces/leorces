package com.leorces.engine.activity.behaviour;

import com.leorces.model.definition.activity.ActivityDefinition;
import com.leorces.model.definition.activity.ActivityType;
import com.leorces.model.runtime.activity.ActivityExecution;

import java.util.List;
import java.util.Map;

public interface ActivityBehavior {

    void run(ActivityExecution activity);

    void complete(ActivityExecution activity, Map<String, Object> variables);

    void terminate(ActivityExecution activity, boolean withInterruption);

    boolean fail(ActivityExecution activity);

    void retry(ActivityExecution activity);

    List<ActivityDefinition> getNextActivities(ActivityExecution activity);

    ActivityType type();

}
