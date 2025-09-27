package com.leorces.engine.activity;

import com.leorces.engine.activity.behaviour.ActivityBehaviorResolver;
import com.leorces.engine.event.activity.cancel.CancelActivitiesByProcessIdEvent;
import com.leorces.engine.event.activity.cancel.CancelActivitiesEvent;
import com.leorces.engine.event.activity.terminate.TerminateActivitiesByProcessIdEvent;
import com.leorces.engine.event.activity.terminate.TerminateActivityByIdAsync;
import com.leorces.engine.event.activity.terminate.TerminateActivityEvent;
import com.leorces.engine.service.TaskExecutorService;
import com.leorces.model.runtime.activity.ActivityExecution;
import com.leorces.persistence.ActivityPersistence;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
class ActivityCancelService {

    private final ActivityPersistence activityPersistence;
    private final ActivityBehaviorResolver behaviorResolver;
    private final ActivityFactory activityFactory;
    private final TaskExecutorService taskExecutor;

    @EventListener
    void handleCancel(CancelActivitiesEvent event) {
        cancel(event.activities);
    }

    @EventListener
    void handleCancel(CancelActivitiesByProcessIdEvent event) {
        cancel(activityPersistence.findActive(event.processId));
    }

    @EventListener
    void handleTerminate(TerminateActivitiesByProcessIdEvent event) {
        terminate(activityPersistence.findActive(event.processId));
    }

    @EventListener
    void handleTerminate(TerminateActivityEvent event) {
        terminate(event.activity);
    }

    @Async
    @EventListener
    void handleTerminate(TerminateActivityByIdAsync event) {
        terminate(activityFactory.getById(event.activityId));
    }

    private void cancel(List<ActivityExecution> activities) {
        var cancellationActivities = activities.stream()
                .map(this::cancelAsync)
                .toArray(CompletableFuture[]::new);
        CompletableFuture.allOf(cancellationActivities).join();
    }

    private void terminate(List<ActivityExecution> activities) {
        var terminatedActivities = activities.stream()
                .map(this::terminateAsync)
                .toArray(CompletableFuture[]::new);
        CompletableFuture.allOf(terminatedActivities).join();
    }

    private void cancel(ActivityExecution activity) {
        behaviorResolver.resolveCancellableStrategy(activity.type())
                .ifPresent(behavior -> behavior.cancel(activity));
    }

    private void terminate(ActivityExecution activity) {
        behaviorResolver.resolveCancellableStrategy(activity.type())
                .ifPresent(behavior -> behavior.terminate(activity));
    }

    private CompletableFuture<Void> cancelAsync(ActivityExecution activity) {
        return taskExecutor.submit(() -> cancel(activity));
    }

    private CompletableFuture<Void> terminateAsync(ActivityExecution activity) {
        return taskExecutor.submit(() -> terminate(activity));
    }

}
