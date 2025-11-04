package com.leorces.engine.activity.behaviour.event.boundary;

import com.leorces.engine.core.CommandDispatcher;
import com.leorces.model.definition.activity.ActivityType;
import com.leorces.model.runtime.activity.ActivityExecution;
import com.leorces.persistence.ActivityPersistence;
import org.springframework.stereotype.Component;

import java.util.Optional;

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

    protected boolean shouldRun(Optional<ActivityExecution> attachedActivity) {
        return attachedActivity.isPresent();
    }

}
