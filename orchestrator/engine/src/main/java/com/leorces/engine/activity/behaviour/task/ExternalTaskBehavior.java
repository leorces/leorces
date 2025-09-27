package com.leorces.engine.activity.behaviour.task;

import com.leorces.engine.activity.behaviour.CancellableActivityBehaviour;
import com.leorces.engine.activity.behaviour.FailableActivityBehavior;
import com.leorces.engine.event.EngineEventBus;
import com.leorces.engine.event.activity.ActivityEvent;
import com.leorces.model.definition.activity.ActivityType;
import com.leorces.model.definition.activity.task.ExternalTask;
import com.leorces.model.runtime.activity.ActivityExecution;
import com.leorces.persistence.ActivityPersistence;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ExternalTaskBehavior implements FailableActivityBehavior, CancellableActivityBehaviour {

    private final ActivityPersistence activityPersistence;
    private final EngineEventBus eventBus;

    @Override
    public void run(ActivityExecution activity) {
        activityPersistence.schedule(activity);
    }

    @Override
    public ActivityExecution complete(ActivityExecution activity) {
        var result = activityPersistence.complete(activity);
        eventBus.publish(ActivityEvent.runAllAsync(result.nextActivities(), result.process()));
        return result;
    }

    @Override
    public void fail(ActivityExecution activity) {
        var externalTaskDefinition = (ExternalTask) activity.definition();

        if (activity.retries() < externalTaskDefinition.retries()) {
            eventBus.publish(ActivityEvent.retryAsync(activity));
            return;
        }

        activityPersistence.fail(activity);
        eventBus.publish(ActivityEvent.incidentFailEvent(activity));
    }

    @Override
    public void retry(ActivityExecution activity) {
        activityPersistence.schedule(incrementRetries(activity));
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
        return ActivityType.EXTERNAL_TASK;
    }

    private ActivityExecution incrementRetries(ActivityExecution activity) {
        return activity.toBuilder()
                .retries(activity.retries() + 1)
                .build();
    }

}
