package com.leorces.engine.activity;

import com.leorces.engine.activity.behaviour.ActivityBehaviorResolver;
import com.leorces.engine.event.activity.fail.FailActivityByIdEventAsync;
import com.leorces.engine.event.activity.fail.FailActivityEventAsync;
import com.leorces.engine.event.activity.retry.RetryActivitiesEventAsync;
import com.leorces.engine.event.activity.retry.RetryActivityByIdEventAsync;
import com.leorces.engine.event.activity.retry.RetryActivityEventAsync;
import com.leorces.engine.service.TaskExecutorService;
import com.leorces.engine.variables.VariableRuntimeService;
import com.leorces.model.runtime.activity.ActivityExecution;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
class ActivityFailService {

    private final VariableRuntimeService variableRuntimeService;
    private final ActivityBehaviorResolver behaviorResolver;
    private final ActivityFactory activityFactory;
    private final TaskExecutorService taskService;

    @Async
    @EventListener
    void handleFail(FailActivityEventAsync event) {
        fail(event.activity);
    }

    @Async
    @EventListener
    void handleFail(FailActivityByIdEventAsync event) {
        fail(activityFactory.getById(event.activityId), event.variables);
    }

    @Async
    @EventListener
    void handleRetry(RetryActivityByIdEventAsync event) {
        retry(activityFactory.getById(event.activityId));
    }

    @Async
    @EventListener
    void handleRetry(RetryActivityEventAsync event) {
        retry(event.activity);
    }

    @Async
    @EventListener
    void handleRetry(RetryActivitiesEventAsync event) {
        event.activities
                .forEach(activity -> taskService.execute(() -> retry(activity)));
    }

    private void fail(ActivityExecution activity) {
        fail(activity, Map.of());
    }

    private void fail(ActivityExecution activity, Map<String, Object> variables) {
        log.debug("Fail {} activity with definitionId: {} and processId: {}", activity.type(), activity.definitionId(), activity.processId());
        variableRuntimeService.setProcessVariables(activity.process(), variables);
        behaviorResolver.resolveFailableStrategy(activity.type())
                .ifPresent(behaviour -> behaviour.fail(activity));
    }

    public void retry(ActivityExecution activity) {
        log.debug("Retry {} activity with definitionId: {} and processId: {}", activity.type(), activity.definitionId(), activity.processId());
        behaviorResolver.resolveFailableStrategy(activity.type())
                .ifPresent(behaviour -> behaviour.retry(activity));
    }

}
