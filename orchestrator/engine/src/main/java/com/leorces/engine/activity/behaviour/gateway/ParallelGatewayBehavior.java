package com.leorces.engine.activity.behaviour.gateway;

import com.leorces.engine.activity.behaviour.AbstractActivityBehavior;
import com.leorces.engine.activity.command.CompleteActivityCommand;
import com.leorces.engine.core.CommandDispatcher;
import com.leorces.model.definition.activity.ActivityDefinition;
import com.leorces.model.definition.activity.ActivityType;
import com.leorces.model.runtime.activity.ActivityExecution;
import com.leorces.persistence.ActivityPersistence;
import org.springframework.stereotype.Component;

@Component
public class ParallelGatewayBehavior extends AbstractActivityBehavior {

    protected ParallelGatewayBehavior(ActivityPersistence activityPersistence,
                                      CommandDispatcher dispatcher) {
        super(activityPersistence, dispatcher);
    }

    @Override
    public void run(ActivityExecution parallelGateway) {
        if (parallelGateway.definition().incoming().size() == 1) {
            dispatcher.dispatchAsync(CompleteActivityCommand.of(parallelGateway));
            return;
        }

        var incomingActivityIds = parallelGateway.previousActivities().stream()
                .map(ActivityDefinition::id)
                .toList();

        if (activityPersistence.isAllCompleted(parallelGateway.processId(), incomingActivityIds)) {
            dispatcher.dispatchAsync(CompleteActivityCommand.of(parallelGateway));
        }
    }

    @Override
    public ActivityType type() {
        return ActivityType.PARALLEL_GATEWAY;
    }

}
