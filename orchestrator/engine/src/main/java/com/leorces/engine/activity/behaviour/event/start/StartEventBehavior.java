package com.leorces.engine.activity.behaviour.event.start;

import com.leorces.engine.activity.behaviour.AbstractActivityBehavior;
import com.leorces.engine.core.CommandDispatcher;
import com.leorces.model.definition.activity.ActivityType;
import com.leorces.persistence.ActivityPersistence;
import org.springframework.stereotype.Component;

@Component
public class StartEventBehavior extends AbstractActivityBehavior {

    protected StartEventBehavior(ActivityPersistence activityPersistence,
                                 CommandDispatcher dispatcher) {
        super(activityPersistence, dispatcher);
    }

    @Override
    public ActivityType type() {
        return ActivityType.START_EVENT;
    }

}
