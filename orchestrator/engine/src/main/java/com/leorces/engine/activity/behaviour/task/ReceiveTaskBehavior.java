package com.leorces.engine.activity.behaviour.task;

import com.leorces.engine.activity.behaviour.AbstractTriggerableCatchBehavior;
import com.leorces.engine.core.CommandDispatcher;
import com.leorces.model.definition.activity.ActivityType;
import com.leorces.model.runtime.activity.ActivityExecution;
import com.leorces.persistence.ActivityPersistence;
import org.springframework.stereotype.Component;

@Component
public class ReceiveTaskBehavior extends AbstractTriggerableCatchBehavior {

    protected ReceiveTaskBehavior(ActivityPersistence activityPersistence,
                                  CommandDispatcher dispatcher) {
        super(activityPersistence, dispatcher);
    }

    @Override
    public void run(ActivityExecution receiveTask) {
        activityPersistence.run(receiveTask);
    }

    @Override
    public ActivityType type() {
        return ActivityType.RECEIVE_TASK;
    }

}
