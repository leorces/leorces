package com.leorces.engine.activity.behaviour.event.start;

import com.leorces.engine.core.CommandDispatcher;
import com.leorces.model.definition.activity.ActivityType;
import com.leorces.persistence.ActivityPersistence;
import org.springframework.stereotype.Component;

@Component
public class EscalationStartEventBehavior extends AbstractTriggerableStartEventBehavior {

    protected EscalationStartEventBehavior(ActivityPersistence activityPersistence,
                                           CommandDispatcher dispatcher) {
        super(activityPersistence, dispatcher);
    }

    @Override
    public ActivityType type() {
        return ActivityType.ESCALATION_START_EVENT;
    }

}
