package com.leorces.engine.activity.behaviour.event.end;

import com.leorces.engine.activity.behaviour.AbstractThrowEscalationBehavior;
import com.leorces.engine.core.CommandDispatcher;
import com.leorces.model.definition.activity.ActivityDefinition;
import com.leorces.model.definition.activity.ActivityType;
import com.leorces.model.runtime.activity.ActivityExecution;
import com.leorces.persistence.ActivityPersistence;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class EscalationEndEventBehavior extends AbstractThrowEscalationBehavior {

    protected EscalationEndEventBehavior(ActivityPersistence activityPersistence,
                                         CommandDispatcher dispatcher) {
        super(activityPersistence, dispatcher);
    }

    @Override
    public List<ActivityDefinition> getNextActivities(ActivityExecution escalationEndEvent) {
        return List.of();
    }

    @Override
    public ActivityType type() {
        return ActivityType.ESCALATION_END_EVENT;
    }

}
