package com.leorces.engine.activity.behaviour.subprocess;

import com.leorces.engine.activity.behaviour.CancellableActivityBehaviour;
import com.leorces.engine.activity.behaviour.FailableActivityBehavior;
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
public class CallActivityBehavior implements CancellableActivityBehaviour, FailableActivityBehavior {

    private final ActivityPersistence activityPersistence;
    private final EngineEventBus eventBus;

    @Override
    public void run(ActivityExecution activity) {
        var result = activityPersistence.run(activity);
        eventBus.publish(ProcessEvent.startByCallActivity(result));
    }

    @Override
    public ActivityExecution complete(ActivityExecution activity) {
        var result = activityPersistence.complete(activity);
        eventBus.publish(ActivityEvent.runAllAsync(result.nextActivities(), result.process()));
        return result;
    }

    @Override
    public void fail(ActivityExecution activity) {
        var result = activityPersistence.fail(activity);
        eventBus.publish(ActivityEvent.incidentFailEvent(result));
    }

    @Override
    public void retry(ActivityExecution activity) {
        var failedActivities = activityPersistence.findFailed(activity.id());
        eventBus.publish(ActivityEvent.retryAllAsync(failedActivities));
    }

    @Override
    public void cancel(ActivityExecution activity) {
        eventBus.publish(ProcessEvent.cancelByIdEvent(activity.id()));
        activityPersistence.cancel(activity);
    }

    @Override
    public void terminate(ActivityExecution activity) {
        eventBus.publish(ProcessEvent.terminateByIdEvent(activity.id()));
        activityPersistence.terminate(activity);
    }

    @Override
    public ActivityType type() {
        return ActivityType.CALL_ACTIVITY;
    }

}
