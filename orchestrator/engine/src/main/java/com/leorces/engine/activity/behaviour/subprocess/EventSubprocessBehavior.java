package com.leorces.engine.activity.behaviour.subprocess;

import com.leorces.engine.activity.behaviour.CancellableActivityBehaviour;
import com.leorces.engine.event.EngineEventBus;
import com.leorces.engine.event.activity.ActivityEvent;
import com.leorces.engine.exception.activity.ActivityNotFoundException;
import com.leorces.model.definition.activity.ActivityDefinition;
import com.leorces.model.definition.activity.ActivityType;
import com.leorces.model.runtime.activity.ActivityExecution;
import com.leorces.persistence.ActivityPersistence;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EventSubprocessBehavior implements CancellableActivityBehaviour {

    private final ActivityPersistence activityPersistence;
    private final EngineEventBus eventBus;

    @Override
    public void run(ActivityExecution activity) {
        var result = activityPersistence.run(activity);
        eventBus.publish(ActivityEvent.runByDefinitionAsync(getStartEvent(result), result.process()));
    }

    @Override
    public ActivityExecution complete(ActivityExecution activity) {
        return activityPersistence.complete(activity);
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
        return ActivityType.EVENT_SUBPROCESS;
    }

    protected ActivityDefinition getStartEvent(ActivityExecution activity) {
        var definitionId = activity.definition().id();
        return activity.processDefinition().activities().stream()
                .filter(activityDefinition -> definitionId.equals(activityDefinition.parentId()))
                .filter(this::isStartEvent)
                .findFirst()
                .orElseThrow(() -> ActivityNotFoundException.startEventNotFoundForSubprocess(activity.definition().id()));
    }

    private boolean isStartEvent(ActivityDefinition activityDefinition) {
        return ActivityType.MESSAGE_START_EVENT.equals(activityDefinition.type())
                || ActivityType.ERROR_START_EVENT.equals(activityDefinition.type());
    }

}
