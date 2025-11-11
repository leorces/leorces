package com.leorces.engine.activity.behaviour.event.boundary;

import com.leorces.engine.core.CommandDispatcher;
import com.leorces.model.definition.activity.ActivityType;
import com.leorces.model.runtime.activity.ActivityExecution;
import com.leorces.persistence.ActivityPersistence;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class EscalationBoundaryEventBehavior extends AbstractBoundaryEventBehavior {

    protected EscalationBoundaryEventBehavior(ActivityPersistence activityPersistence,
                                              CommandDispatcher dispatcher) {
        super(activityPersistence, dispatcher);
    }

    @Override
    public ActivityType type() {
        return ActivityType.ESCALATION_BOUNDARY_EVENT;
    }

    protected boolean shouldRun(Optional<ActivityExecution> attachedActivity) {
        return attachedActivity.isPresent();
    }

}
