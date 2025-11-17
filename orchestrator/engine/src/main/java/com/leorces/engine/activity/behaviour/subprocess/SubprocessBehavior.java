package com.leorces.engine.activity.behaviour.subprocess;


import com.leorces.engine.core.CommandDispatcher;
import com.leorces.model.definition.activity.ActivityType;
import com.leorces.model.runtime.activity.ActivityExecution;
import com.leorces.persistence.ActivityPersistence;
import org.springframework.stereotype.Component;

import java.util.Map;


@Component
public class SubprocessBehavior extends AbstractSubprocessBehavior {

    protected SubprocessBehavior(ActivityPersistence activityPersistence,
                                 CommandDispatcher dispatcher) {
        super(activityPersistence, dispatcher);
    }

    @Override
    public void complete(ActivityExecution subprocess, Map<String, Object> variables) {
        if (!isAllChildActivitiesCompleted(subprocess)) {
            return;
        }

        var completedSubprocess = activityPersistence.complete(subprocess);
        postComplete(completedSubprocess, variables);
    }

    @Override
    public ActivityType type() {
        return ActivityType.SUBPROCESS;
    }

}
