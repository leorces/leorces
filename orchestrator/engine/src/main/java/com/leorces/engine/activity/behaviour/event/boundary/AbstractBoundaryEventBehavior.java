package com.leorces.engine.activity.behaviour.event.boundary;

import com.leorces.engine.activity.behaviour.TriggerableActivityBehaviour;
import com.leorces.engine.event.EngineEventBus;
import com.leorces.engine.event.activity.ActivityEvent;
import com.leorces.model.definition.activity.ActivityDefinition;
import com.leorces.model.definition.activity.BoundaryEventDefinition;
import com.leorces.model.runtime.activity.ActivityExecution;
import com.leorces.model.runtime.activity.ActivityState;
import com.leorces.model.runtime.process.Process;
import com.leorces.persistence.ActivityPersistence;

import java.util.Optional;

public abstract class AbstractBoundaryEventBehavior implements TriggerableActivityBehaviour {

    private final ActivityPersistence activityPersistence;
    private final EngineEventBus eventBus;

    protected AbstractBoundaryEventBehavior(ActivityPersistence activityPersistence,
                                            EngineEventBus eventBus) {
        this.activityPersistence = activityPersistence;
        this.eventBus = eventBus;
    }

    @Override
    public void run(ActivityExecution activity) {
        var boundaryEvent = (BoundaryEventDefinition) activity.definition();
        var attachedActivity = activityPersistence.findByDefinitionId(activity.processId(), boundaryEvent.attachedToRef());

        if (!shouldRun(attachedActivity)) {
            return;
        }

        var result = activityPersistence.run(activity);

        if (boundaryEvent.cancelActivity()) {
            eventBus.publish(ActivityEvent.terminate(attachedActivity.get()));
        }

        eventBus.publish(ActivityEvent.completeAsync(result));
    }

    @Override
    public ActivityExecution complete(ActivityExecution activity) {
        var result = activityPersistence.complete(activity);
        eventBus.publish(ActivityEvent.runAllAsync(result.nextActivities(), result.process()));
        return result;
    }

    @Override
    public void trigger(Process process, ActivityDefinition definition) {
        eventBus.publish(ActivityEvent.runByDefinitionAsync(definition, process));
    }

    protected boolean shouldRun(Optional<ActivityExecution> attachedActivity) {
        return attachedActivity.isPresent() && attachedActivity.get().state() == ActivityState.ACTIVE;
    }

}
