package com.leorces.engine.activity.behaviour.event.boundary;

import com.leorces.engine.core.CommandDispatcher;
import com.leorces.model.definition.activity.ActivityType;
import com.leorces.persistence.ActivityPersistence;
import org.springframework.stereotype.Component;

@Component
public class MessageBoundaryEventBehavior extends AbstractBoundaryEventBehavior {

    protected MessageBoundaryEventBehavior(ActivityPersistence activityPersistence,
                                           CommandDispatcher dispatcher) {
        super(activityPersistence, dispatcher);
    }

    @Override
    public ActivityType type() {
        return ActivityType.MESSAGE_BOUNDARY_EVENT;
    }

}
