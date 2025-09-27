package com.leorces.engine.activity.behaviour.event;

import com.leorces.engine.activity.behaviour.CancellableActivityBehaviour;
import com.leorces.engine.event.EngineEventBus;
import com.leorces.engine.event.activity.ActivityEvent;
import com.leorces.model.definition.activity.ActivityType;
import com.leorces.model.runtime.activity.ActivityExecution;
import com.leorces.persistence.ActivityPersistence;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MessageIntermediateCatchEventBehavior implements CancellableActivityBehaviour {

    private final ActivityPersistence activityPersistence;
    private final EngineEventBus eventBus;

    @Override
    public void run(ActivityExecution activity) {
        activityPersistence.run(activity);
    }

    @Override
    public ActivityExecution complete(ActivityExecution activity) {
        var result = activityPersistence.complete(activity);
        eventBus.publish(ActivityEvent.runAllAsync(result.nextActivities(), result.process()));
        return result;
    }

    @Override
    public void cancel(ActivityExecution activity) {
        activityPersistence.cancel(activity);
    }

    @Override
    public void terminate(ActivityExecution activity) {
        activityPersistence.terminate(activity);
    }

    @Override
    public ActivityType type() {
        return ActivityType.MESSAGE_INTERMEDIATE_CATCH_EVENT;
    }

}
