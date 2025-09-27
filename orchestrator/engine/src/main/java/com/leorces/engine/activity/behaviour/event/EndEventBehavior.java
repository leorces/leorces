package com.leorces.engine.activity.behaviour.event;

import com.leorces.engine.activity.behaviour.ActivityBehavior;
import com.leorces.engine.event.EngineEventBus;
import com.leorces.engine.event.activity.ActivityEvent;
import com.leorces.engine.event.process.ProcessEvent;
import com.leorces.model.definition.activity.ActivityType;
import com.leorces.model.runtime.activity.ActivityExecution;
import com.leorces.persistence.ActivityPersistence;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EndEventBehavior implements ActivityBehavior {

    private final ActivityPersistence activityPersistence;
    private final EngineEventBus eventBus;

    @Override
    public void run(ActivityExecution activity) {
        eventBus.publish(ActivityEvent.completeAsync(activity));
    }

    @Override
    public ActivityExecution complete(ActivityExecution activity) {
        var result = activityPersistence.complete(activity);
        if (result.parentDefinitionId() == null) {
            eventBus.publish(ProcessEvent.completeAsync(result.process()));
        } else {
            eventBus.publish(ActivityEvent.completeByDefinitionIdAsync(result.parentDefinitionId(), result.processId()));
        }
        return result;
    }

    @Override
    public ActivityType type() {
        return ActivityType.END_EVENT;
    }

}
