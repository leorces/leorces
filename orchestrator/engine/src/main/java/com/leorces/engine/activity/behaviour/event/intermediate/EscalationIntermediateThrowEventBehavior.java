package com.leorces.engine.activity.behaviour.event.intermediate;

import com.leorces.engine.activity.behaviour.AbstractThrowEscalationBehavior;
import com.leorces.engine.core.CommandDispatcher;
import com.leorces.engine.service.resolver.EscalationHandlerResolver;
import com.leorces.model.definition.activity.ActivityType;
import com.leorces.persistence.ActivityPersistence;
import org.springframework.stereotype.Component;

@Component
public class EscalationIntermediateThrowEventBehavior extends AbstractThrowEscalationBehavior {

    protected EscalationIntermediateThrowEventBehavior(ActivityPersistence activityPersistence,
                                                       CommandDispatcher dispatcher,
                                                       EscalationHandlerResolver escalationHandlerResolver) {
        super(activityPersistence, dispatcher, escalationHandlerResolver);
    }

    @Override
    public ActivityType type() {
        return ActivityType.ESCALATION_INTERMEDIATE_THROW_EVENT;
    }

}
