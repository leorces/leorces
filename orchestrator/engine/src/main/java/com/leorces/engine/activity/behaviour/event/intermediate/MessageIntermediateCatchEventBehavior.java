package com.leorces.engine.activity.behaviour.event.intermediate;

import com.leorces.engine.activity.behaviour.AbstractTriggerableCatchBehavior;
import com.leorces.engine.core.CommandDispatcher;
import com.leorces.model.definition.activity.ActivityType;
import com.leorces.model.runtime.activity.ActivityExecution;
import com.leorces.persistence.ActivityPersistence;
import org.springframework.stereotype.Component;

@Component
public class MessageIntermediateCatchEventBehavior extends AbstractTriggerableCatchBehavior {

    protected MessageIntermediateCatchEventBehavior(ActivityPersistence activityPersistence,
                                                    CommandDispatcher dispatcher) {
        super(activityPersistence, dispatcher);
    }

    @Override
    public void run(ActivityExecution messageIntermediateCatchEvent) {
        activityPersistence.run(messageIntermediateCatchEvent);
    }

    @Override
    public ActivityType type() {
        return ActivityType.MESSAGE_INTERMEDIATE_CATCH_EVENT;
    }

}
