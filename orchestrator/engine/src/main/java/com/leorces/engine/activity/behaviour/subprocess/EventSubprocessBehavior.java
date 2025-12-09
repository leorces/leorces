package com.leorces.engine.activity.behaviour.subprocess;

import com.leorces.engine.core.CommandDispatcher;
import com.leorces.model.definition.activity.ActivityDefinition;
import com.leorces.model.definition.activity.ActivityType;
import com.leorces.model.definition.activity.event.start.StartEventActivityDefinition;
import com.leorces.model.runtime.activity.ActivityExecution;
import com.leorces.persistence.ActivityPersistence;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class EventSubprocessBehavior extends AbstractSubprocessBehavior {

    protected EventSubprocessBehavior(ActivityPersistence activityPersistence,
                                      CommandDispatcher dispatcher) {
        super(activityPersistence, dispatcher);
    }

    @Override
    public void complete(ActivityExecution eventSubprocess, Map<String, Object> variables) {
        if (!isAllChildActivitiesCompleted(eventSubprocess)) {
            return;
        }

        var completedEventSubprocess = activityPersistence.complete(eventSubprocess);

        var startEvent = (StartEventActivityDefinition) getStartEvent(completedEventSubprocess);
        if (startEvent.isInterrupting()) {
            postComplete(completedEventSubprocess, variables);
        }
    }

    @Override
    public List<ActivityDefinition> getNextActivities(ActivityExecution eventSubprocess) {
        return List.of();
    }

    @Override
    public ActivityType type() {
        return ActivityType.EVENT_SUBPROCESS;
    }

}
