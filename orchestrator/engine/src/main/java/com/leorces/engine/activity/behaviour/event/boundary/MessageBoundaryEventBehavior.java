package com.leorces.engine.activity.behaviour.event.boundary;

import com.leorces.engine.event.EngineEventBus;
import com.leorces.model.definition.activity.ActivityType;
import com.leorces.persistence.ActivityPersistence;
import org.springframework.stereotype.Component;

@Component
public class MessageBoundaryEventBehavior extends AbstractBoundaryEventBehavior {

    protected MessageBoundaryEventBehavior(ActivityPersistence activityPersistence,
                                           EngineEventBus eventBus) {
        super(activityPersistence, eventBus);
    }

    @Override
    public ActivityType type() {
        return ActivityType.MESSAGE_BOUNDARY_EVENT;
    }

}
