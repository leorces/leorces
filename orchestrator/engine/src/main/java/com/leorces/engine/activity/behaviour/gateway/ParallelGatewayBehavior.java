package com.leorces.engine.activity.behaviour.gateway;

import com.leorces.engine.activity.behaviour.ActivityBehavior;
import com.leorces.engine.event.EngineEventBus;
import com.leorces.engine.event.activity.ActivityEvent;
import com.leorces.model.definition.activity.ActivityDefinition;
import com.leorces.model.definition.activity.ActivityType;
import com.leorces.model.runtime.activity.ActivityExecution;
import com.leorces.persistence.ActivityPersistence;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ParallelGatewayBehavior implements ActivityBehavior {

    private final ActivityPersistence activityPersistence;
    private final EngineEventBus eventBus;

    @Override
    public void run(ActivityExecution activity) {
        if (activity.definition().incoming().size() == 1) {
            eventBus.publish(ActivityEvent.completeAsync(activity));
            return;
        }

        var incomingActivityIds = activity.previousActivities().stream()
                .map(ActivityDefinition::id)
                .toList();

        if (activityPersistence.isAllCompleted(activity.processId(), incomingActivityIds)) {
            eventBus.publish(ActivityEvent.completeAsync(activity));
        }
    }

    @Override
    public ActivityExecution complete(ActivityExecution activity) {
        var result = activityPersistence.complete(activity);
        eventBus.publish(ActivityEvent.runAllAsync(result.nextActivities(), result.process()));
        return result;
    }

    @Override
    public ActivityType type() {
        return ActivityType.PARALLEL_GATEWAY;
    }

}
