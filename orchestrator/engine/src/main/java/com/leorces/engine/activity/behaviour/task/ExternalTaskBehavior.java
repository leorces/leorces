package com.leorces.engine.activity.behaviour.task;

import com.leorces.engine.activity.behaviour.AbstractExternalTaskBehavior;
import com.leorces.engine.configuration.properties.EngineProperties;
import com.leorces.engine.core.CommandDispatcher;
import com.leorces.model.definition.activity.ActivityType;
import com.leorces.persistence.ActivityPersistence;
import org.springframework.stereotype.Component;

@Component
public class ExternalTaskBehavior extends AbstractExternalTaskBehavior {

    protected ExternalTaskBehavior(ActivityPersistence activityPersistence,
                                   CommandDispatcher dispatcher,
                                   EngineProperties engineProperties) {
        super(activityPersistence, dispatcher, engineProperties);
    }

    @Override
    public ActivityType type() {
        return ActivityType.EXTERNAL_TASK;
    }

}
