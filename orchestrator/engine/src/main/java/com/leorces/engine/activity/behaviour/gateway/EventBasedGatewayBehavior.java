package com.leorces.engine.activity.behaviour.gateway;

import com.leorces.engine.activity.behaviour.ActivityEventListenerBehavior;
import com.leorces.engine.activity.behaviour.CancellableActivityBehaviour;
import com.leorces.engine.event.EngineEventBus;
import com.leorces.engine.event.activity.ActivityEvent;
import com.leorces.engine.event.activity.complete.CompleteActivitySuccessEvent;
import com.leorces.model.definition.activity.ActivityDefinition;
import com.leorces.model.definition.activity.ActivityType;
import com.leorces.model.runtime.activity.ActivityExecution;
import com.leorces.persistence.ActivityPersistence;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventBasedGatewayBehavior implements CancellableActivityBehaviour, ActivityEventListenerBehavior {

    private final ActivityPersistence activityPersistence;
    private final EngineEventBus eventBus;

    @Async
    @EventListener
    public void handle(ActivityEvent event) {
        if (event instanceof CompleteActivitySuccessEvent) {
            var activity = ((CompleteActivitySuccessEvent) event).activity;
            findEventBasedGateway(activity)
                    .map(definition -> ActivityEvent.completeByDefinitionIdAsync(definition.id(), activity.processId()))
                    .ifPresent(eventBus::publish);
        }
    }

    @Override
    public void run(ActivityExecution activity) {
        var result = activityPersistence.run(activity);
        eventBus.publish(ActivityEvent.runAllAsync(result.nextActivities(), result.process()));
    }

    @Override
    public ActivityExecution complete(ActivityExecution activity) {
        cancelActiveActivities(activity);
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
        return ActivityType.EVENT_BASED_GATEWAY;
    }

    private void cancelActiveActivities(ActivityExecution activity) {
        var activities = activityPersistence.findActive(activity.processId(), activity.outgoing());
        eventBus.publish(ActivityEvent.cancelAll(activities));
    }

    private Optional<ActivityDefinition> findEventBasedGateway(ActivityExecution activity) {
        return activity.previousActivities().stream()
                .filter(this::isEventBasedGateway)
                .findFirst();
    }

    private boolean isEventBasedGateway(ActivityDefinition activityDefinition) {
        return ActivityType.EVENT_BASED_GATEWAY.equals(activityDefinition.type());
    }

}
