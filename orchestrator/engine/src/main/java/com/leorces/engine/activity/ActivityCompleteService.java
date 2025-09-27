package com.leorces.engine.activity;

import com.leorces.engine.activity.behaviour.ActivityBehaviorResolver;
import com.leorces.engine.event.EngineEventBus;
import com.leorces.engine.event.activity.ActivityEvent;
import com.leorces.engine.event.activity.complete.CompleteActivityAsync;
import com.leorces.engine.event.activity.complete.CompleteActivityByDefinitionIdEventAsync;
import com.leorces.engine.event.activity.complete.CompleteActivityByIdEventAsync;
import com.leorces.engine.variables.VariableRuntimeService;
import com.leorces.model.runtime.activity.ActivityExecution;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
class ActivityCompleteService {

    private final VariableRuntimeService variableRuntimeService;
    private final ActivityBehaviorResolver behaviorResolver;
    private final ActivityFactory activityFactory;
    private final EngineEventBus eventBus;

    @Async
    @EventListener
    void handleComplete(CompleteActivityAsync event) {
        complete(event.activity);
    }

    @Async
    @EventListener
    void handleComplete(CompleteActivityByIdEventAsync event) {
        complete(activityFactory.getById(event.activityId), event.variables);
    }

    @Async
    @EventListener
    void handleComplete(CompleteActivityByDefinitionIdEventAsync event) {
        complete(activityFactory.getByDefinitionId(event.definitionId, event.processId));
    }

    private void complete(ActivityExecution activity) {
        complete(activity, Map.of());
    }

    private void complete(ActivityExecution activity, Map<String, Object> variables) {
        log.debug("Complete {} activity with definitionId: {} and processId: {}", activity.type(), activity.definitionId(), activity.processId());
        try {
            var result = behaviorResolver.resolveStrategy(activity.type()).complete(activity);
            processVariables(result, variables);
            eventBus.publish(ActivityEvent.completeSuccess(result));
        } catch (Exception e) {
            eventBus.publish(ActivityEvent.failAsync(activity));
        }
    }

    private void processVariables(ActivityExecution activity, Map<String, Object> variables) {
        var outputVariables = variableRuntimeService.evaluate(activity, activity.outputs());
        var outputVariablesMap = variableRuntimeService.toMap(outputVariables);
        var variablesToSet = new HashMap<>(variables);
        variablesToSet.putAll(outputVariablesMap);
        variableRuntimeService.setProcessVariables(activity.process(), variablesToSet);
    }

}
