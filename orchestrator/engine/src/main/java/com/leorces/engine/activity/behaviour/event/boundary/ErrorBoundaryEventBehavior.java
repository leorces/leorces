package com.leorces.engine.activity.behaviour.event.boundary;

import com.leorces.engine.event.EngineEventBus;
import com.leorces.model.definition.activity.ActivityType;
import com.leorces.persistence.ActivityPersistence;
import org.springframework.stereotype.Component;

@Component
public class ErrorBoundaryEventBehavior extends AbstractBoundaryEventBehavior {

    protected ErrorBoundaryEventBehavior(ActivityPersistence activityPersistence,
                                         EngineEventBus eventBus) {
        super(activityPersistence, eventBus);
    }

    @Override
    public ActivityType type() {
        return ActivityType.ERROR_BOUNDARY_EVENT;
    }

}
