package com.leorces.engine.activity.behaviour.subprocess;


import com.leorces.engine.core.CommandDispatcher;
import com.leorces.engine.exception.activity.ActivityNotFoundException;
import com.leorces.model.definition.activity.ActivityDefinition;
import com.leorces.model.definition.activity.ActivityType;
import com.leorces.model.runtime.activity.ActivityExecution;
import com.leorces.persistence.ActivityPersistence;
import org.springframework.stereotype.Component;


@Component
public class SubprocessBehavior extends AbstractSubprocessBehavior {

    protected SubprocessBehavior(ActivityPersistence activityPersistence,
                                 CommandDispatcher dispatcher) {
        super(activityPersistence, dispatcher);
    }

    @Override
    protected ActivityDefinition getStartEvent(ActivityExecution activity) {
        var definitionId = activity.definition().id();
        return activity.processDefinition().activities().stream()
                .filter(activityDefinition -> definitionId.equals(activityDefinition.parentId()))
                .filter(activityDefinition -> activityDefinition.type() == ActivityType.START_EVENT)
                .findFirst()
                .orElseThrow(() -> ActivityNotFoundException.startEventNotFoundForSubprocess(activity.definition().id()));
    }

    @Override
    public ActivityType type() {
        return ActivityType.SUBPROCESS;
    }

}
