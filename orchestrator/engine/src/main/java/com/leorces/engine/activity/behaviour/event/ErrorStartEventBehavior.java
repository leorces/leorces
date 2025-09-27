package com.leorces.engine.activity.behaviour.event;

import com.leorces.engine.activity.behaviour.TriggerableActivityBehaviour;
import com.leorces.engine.event.EngineEventBus;
import com.leorces.engine.event.activity.ActivityEvent;
import com.leorces.engine.exception.activity.ActivityNotFoundException;
import com.leorces.model.definition.activity.ActivityDefinition;
import com.leorces.model.definition.activity.ActivityType;
import com.leorces.model.runtime.activity.ActivityExecution;
import com.leorces.model.runtime.process.Process;
import com.leorces.persistence.ActivityPersistence;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ErrorStartEventBehavior implements TriggerableActivityBehaviour {

    private final ActivityPersistence activityPersistence;
    private final EngineEventBus eventBus;

    @Override
    public void trigger(Process process, ActivityDefinition definition) {
        var eventSubprocess = getEventSubprocess(process, definition);
        eventBus.publish(ActivityEvent.runByDefinitionAsync(eventSubprocess, process));
    }

    @Override
    public void run(ActivityExecution activity) {
        eventBus.publish(ActivityEvent.completeAsync(activity));
    }

    @Override
    public ActivityExecution complete(ActivityExecution activity) {
        var result = activityPersistence.complete(activity);
        eventBus.publish(ActivityEvent.runAllAsync(result.nextActivities(), result.process()));
        return result;
    }

    @Override
    public ActivityType type() {
        return ActivityType.ERROR_START_EVENT;
    }

    private ActivityDefinition getEventSubprocess(Process process, ActivityDefinition definition) {
        return process.definition().getActivityById(definition.parentId())
                .orElseThrow(ActivityNotFoundException::eventSubprocessNotFound);
    }

}
