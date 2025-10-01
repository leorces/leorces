package com.leorces.engine.activity.behaviour.subprocess;

import com.leorces.engine.core.CommandDispatcher;
import com.leorces.engine.exception.activity.ActivityNotFoundException;
import com.leorces.model.definition.activity.ActivityDefinition;
import com.leorces.model.definition.activity.ActivityType;
import com.leorces.model.runtime.activity.ActivityExecution;
import com.leorces.persistence.ActivityPersistence;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class EventSubprocessBehavior extends AbstractSubprocessBehavior {

    protected EventSubprocessBehavior(ActivityPersistence activityPersistence,
                                      CommandDispatcher dispatcher) {
        super(activityPersistence, dispatcher);
    }

    @Override
    public List<ActivityDefinition> getNextActivities(ActivityExecution activity) {
        return List.of();
    }

    @Override
    protected ActivityDefinition getStartEvent(ActivityExecution activity) {
        var definitionId = activity.definition().id();
        return activity.processDefinition().activities().stream()
                .filter(activityDefinition -> definitionId.equals(activityDefinition.parentId()))
                .filter(this::isStartEvent)
                .findFirst()
                .orElseThrow(() -> ActivityNotFoundException.startEventNotFoundForSubprocess(activity.definition().id()));
    }

    @Override
    public ActivityType type() {
        return ActivityType.EVENT_SUBPROCESS;
    }

    private boolean isStartEvent(ActivityDefinition activityDefinition) {
        return ActivityType.MESSAGE_START_EVENT.equals(activityDefinition.type())
                || ActivityType.ERROR_START_EVENT.equals(activityDefinition.type());
    }

}
