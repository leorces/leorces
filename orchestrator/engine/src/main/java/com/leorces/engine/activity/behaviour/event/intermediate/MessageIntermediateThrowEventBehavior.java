package com.leorces.engine.activity.behaviour.event.intermediate;

import com.leorces.engine.activity.behaviour.AbstractThrowMessageBehavior;
import com.leorces.engine.configuration.properties.EngineProperties;
import com.leorces.engine.core.CommandDispatcher;
import com.leorces.model.definition.activity.ActivityType;
import com.leorces.persistence.ActivityPersistence;
import org.springframework.stereotype.Component;

@Component
public class MessageIntermediateThrowEventBehavior extends AbstractThrowMessageBehavior {

    protected MessageIntermediateThrowEventBehavior(ActivityPersistence activityPersistence,
                                                    CommandDispatcher dispatcher,
                                                    EngineProperties engineProperties) {
        super(activityPersistence, dispatcher, engineProperties);
    }

    @Override
    public ActivityType type() {
        return ActivityType.MESSAGE_INTERMEDIATE_THROW_EVENT;
    }

}
