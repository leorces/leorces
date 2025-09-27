package com.leorces.engine.activity.behaviour.gateway;

import com.leorces.engine.event.EngineEventBus;
import com.leorces.engine.event.activity.ActivityEvent;
import com.leorces.engine.exception.activity.GatewayException;
import com.leorces.engine.variables.VariableRuntimeService;
import com.leorces.juel.ExpressionEvaluator;
import com.leorces.model.definition.activity.ActivityDefinition;
import com.leorces.model.definition.activity.ActivityType;
import com.leorces.model.definition.activity.gateway.ExclusiveGateway;
import com.leorces.model.runtime.activity.ActivityExecution;
import com.leorces.persistence.ActivityPersistence;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class ExclusiveGatewayBehavior extends AbstractGatewayBehavior {

    private final VariableRuntimeService variableRuntimeService;

    protected ExclusiveGatewayBehavior(ExpressionEvaluator expressionEvaluator,
                                       VariableRuntimeService variableRuntimeService,
                                       ActivityPersistence activityPersistence,
                                       EngineEventBus eventBus) {
        super(activityPersistence, expressionEvaluator, eventBus);
        this.variableRuntimeService = variableRuntimeService;
    }

    @Override
    public void run(ActivityExecution activity) {
        eventBus.publish(ActivityEvent.completeAsync(activity));
    }

    @Override
    public ActivityExecution complete(ActivityExecution activity) {
        var variables = variableRuntimeService.getScopedVariables(activity);
        var nextActivities = getNextActivities(activity, variables);

        if (nextActivities.size() != 1) {
            throw GatewayException.noValidPath(activity);
        }

        var result = activityPersistence.complete(activity);
        eventBus.publish(ActivityEvent.runByDefinitionAsync(nextActivities.getFirst(), result.process()));
        return result;
    }

    @Override
    public ActivityType type() {
        return ActivityType.EXCLUSIVE_GATEWAY;
    }

    private List<ActivityDefinition> getNextActivities(ActivityExecution activity, Map<String, Object> variables) {
        var exclusiveGateway = (ExclusiveGateway) activity.definition();
        var nextActivityIds = evaluateExclusiveConditions(exclusiveGateway.condition(), variables);
        return getNextActivities(activity.processDefinition(), nextActivityIds);
    }

}
