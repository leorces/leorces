package com.leorces.engine.activity.behaviour.gateway;

import com.leorces.engine.activity.behaviour.AbstractActivityBehavior;
import com.leorces.engine.core.CommandDispatcher;
import com.leorces.model.definition.activity.ActivityType;
import com.leorces.persistence.ActivityPersistence;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class EventBasedGatewayBehavior extends AbstractActivityBehavior {

    protected EventBasedGatewayBehavior(ActivityPersistence activityPersistence,
                                        CommandDispatcher dispatcher) {
        super(activityPersistence, dispatcher);
    }

    @Override
    public ActivityType type() {
        return ActivityType.EVENT_BASED_GATEWAY;
    }

}
