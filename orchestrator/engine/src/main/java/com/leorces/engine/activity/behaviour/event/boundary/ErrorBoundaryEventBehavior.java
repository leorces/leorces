package com.leorces.engine.activity.behaviour.event.boundary;

import com.leorces.engine.activity.behaviour.AbstractBoundaryEventBehavior;
import com.leorces.engine.core.CommandDispatcher;
import com.leorces.model.definition.activity.ActivityType;
import com.leorces.persistence.ActivityPersistence;
import org.springframework.stereotype.Component;

@Component
public class ErrorBoundaryEventBehavior extends AbstractBoundaryEventBehavior {

    protected ErrorBoundaryEventBehavior(ActivityPersistence activityPersistence,
                                         CommandDispatcher dispatcher) {
        super(activityPersistence, dispatcher);
    }

    @Override
    public ActivityType type() {
        return ActivityType.ERROR_BOUNDARY_EVENT;
    }

}
