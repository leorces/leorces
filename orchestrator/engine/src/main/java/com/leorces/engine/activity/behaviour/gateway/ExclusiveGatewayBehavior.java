package com.leorces.engine.activity.behaviour.gateway;

import com.leorces.engine.core.CommandDispatcher;
import com.leorces.engine.exception.activity.GatewayException;
import com.leorces.engine.variables.VariablesService;
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

    private final VariablesService variablesService;

    protected ExclusiveGatewayBehavior(ExpressionEvaluator expressionEvaluator,
                                       VariablesService variablesService,
                                       ActivityPersistence activityPersistence,
                                       CommandDispatcher dispatcher) {
        super(activityPersistence, expressionEvaluator, dispatcher);
        this.variablesService = variablesService;
    }

    @Override
    public void complete(ActivityExecution activity, Map<String, Object> variables) {
        var nextActivities = getNextActivities(activity);

        if (nextActivities.size() != 1) {
            throw GatewayException.noValidPath(activity);
        }

        var completedGateway = activityPersistence.complete(activity);
        handleActivityCompletion(completedGateway, nextActivities);
    }

    @Override
    public List<ActivityDefinition> getNextActivities(ActivityExecution activity) {
        var variables = variablesService.getScopedVariables(activity);
        var exclusiveGateway = (ExclusiveGateway) activity.definition();
        var nextActivityIds = evaluateExclusiveConditions(exclusiveGateway.condition(), variables);
        return getNextActivities(activity.processDefinition(), nextActivityIds);
    }

    @Override
    public ActivityType type() {
        return ActivityType.EXCLUSIVE_GATEWAY;
    }

}
