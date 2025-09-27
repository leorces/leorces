package com.leorces.engine.activity.behaviour.event;

import com.leorces.engine.activity.behaviour.CancellableActivityBehaviour;
import com.leorces.engine.activity.behaviour.TriggerableActivityBehaviour;
import com.leorces.engine.event.EngineEventBus;
import com.leorces.engine.event.activity.ActivityEvent;
import com.leorces.engine.variables.VariableRuntimeService;
import com.leorces.juel.ExpressionEvaluator;
import com.leorces.model.definition.activity.ActivityDefinition;
import com.leorces.model.definition.activity.ActivityType;
import com.leorces.model.definition.activity.event.IntermediateCatchEvent;
import com.leorces.model.runtime.activity.ActivityExecution;
import com.leorces.model.runtime.process.Process;
import com.leorces.persistence.ActivityPersistence;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class IntermediateCatchEventBehavior implements TriggerableActivityBehaviour, CancellableActivityBehaviour {

    private final VariableRuntimeService variableRuntimeService;
    private final ActivityPersistence activityPersistence;
    private final ExpressionEvaluator expressionEvaluator;
    private final EngineEventBus eventBus;

    @Override
    public void trigger(Process process, ActivityDefinition definition) {
        activityPersistence.findByDefinitionId(process.id(), definition.id())
                .ifPresent(activity -> eventBus.publish(ActivityEvent.completeAsync(activity)));
    }

    @Override
    public void run(ActivityExecution activity) {
        var result = activityPersistence.run(activity);

        if (isConditionMatched(result)) {
            eventBus.publish(ActivityEvent.completeAsync(result));
        }
    }

    @Override
    public ActivityExecution complete(ActivityExecution activity) {
        var result = activityPersistence.complete(activity);
        eventBus.publish(ActivityEvent.runAllAsync(result.nextActivities(), result.process()));
        return result;
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
        return ActivityType.INTERMEDIATE_CATCH_EVENT;
    }

    private boolean isConditionMatched(ActivityExecution activity) {
        var variables = variableRuntimeService.getScopedVariables(activity);
        var definition = (IntermediateCatchEvent) activity.definition();
        return expressionEvaluator.evaluateBoolean(definition.condition(), variables);
    }

}
