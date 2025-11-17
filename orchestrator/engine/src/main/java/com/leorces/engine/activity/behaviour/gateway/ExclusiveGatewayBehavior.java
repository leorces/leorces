package com.leorces.engine.activity.behaviour.gateway;

import com.leorces.engine.activity.behaviour.AbstractConditionalGatewayBehavior;
import com.leorces.engine.core.CommandDispatcher;
import com.leorces.engine.exception.activity.GatewayException;
import com.leorces.engine.service.variable.VariablesService;
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
public class ExclusiveGatewayBehavior extends AbstractConditionalGatewayBehavior {

    private final VariablesService variablesService;

    protected ExclusiveGatewayBehavior(ExpressionEvaluator expressionEvaluator,
                                       VariablesService variablesService,
                                       ActivityPersistence activityPersistence,
                                       CommandDispatcher dispatcher) {
        super(activityPersistence, expressionEvaluator, dispatcher);
        this.variablesService = variablesService;
    }

    @Override
    public void complete(ActivityExecution exclusiveGateway, Map<String, Object> variables) {
        var nextActivities = getNextActivities(exclusiveGateway);

        if (nextActivities.size() != 1) {
            throw GatewayException.noValidPath(exclusiveGateway);
        }

        var completedExclusiveGateway = activityPersistence.complete(exclusiveGateway);
        handleActivityCompletion(completedExclusiveGateway, nextActivities);
    }

    @Override
    public List<ActivityDefinition> getNextActivities(ActivityExecution exclusiveGateway) {
        var variables = variablesService.getScopedVariables(exclusiveGateway);
        var condition = ((ExclusiveGateway) exclusiveGateway.definition()).condition();
        var nextActivityIds = evaluateExclusiveConditions(condition, variables);
        return getNextActivities(exclusiveGateway.processDefinition(), nextActivityIds);
    }

    @Override
    public ActivityType type() {
        return ActivityType.EXCLUSIVE_GATEWAY;
    }

}
