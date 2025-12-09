package com.leorces.engine.activity.behaviour.event.end;

import com.leorces.engine.activity.behaviour.AbstractThrowMessageBehavior;
import com.leorces.engine.configuration.properties.EngineProperties;
import com.leorces.engine.core.CommandDispatcher;
import com.leorces.model.definition.activity.ActivityDefinition;
import com.leorces.model.definition.activity.ActivityType;
import com.leorces.model.runtime.activity.ActivityExecution;
import com.leorces.persistence.ActivityPersistence;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class MessageEndEventBehavior extends AbstractThrowMessageBehavior {

    protected MessageEndEventBehavior(ActivityPersistence activityPersistence,
                                      CommandDispatcher dispatcher,
                                      EngineProperties engineProperties) {
        super(activityPersistence, dispatcher, engineProperties);
    }

    @Override
    public List<ActivityDefinition> getNextActivities(ActivityExecution endEvent) {
        return List.of();
    }

    @Override
    public ActivityType type() {
        return ActivityType.MESSAGE_END_EVENT;
    }

}
