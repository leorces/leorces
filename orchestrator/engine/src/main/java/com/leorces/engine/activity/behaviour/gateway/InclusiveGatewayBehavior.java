package com.leorces.engine.activity.behaviour.gateway;

import com.leorces.engine.core.CommandDispatcher;
import com.leorces.engine.exception.activity.GatewayException;
import com.leorces.engine.variables.VariablesService;
import com.leorces.juel.ExpressionEvaluator;
import com.leorces.model.definition.activity.ActivityDefinition;
import com.leorces.model.definition.activity.ActivityType;
import com.leorces.model.definition.activity.gateway.InclusiveGateway;
import com.leorces.model.runtime.activity.ActivityExecution;
import com.leorces.persistence.ActivityPersistence;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class InclusiveGatewayBehavior extends AbstractGatewayBehavior {

    private final VariablesService variablesService;

    protected InclusiveGatewayBehavior(ExpressionEvaluator expressionEvaluator,
                                       VariablesService variablesService,
                                       ActivityPersistence activityPersistence,
                                       CommandDispatcher dispatcher) {
        super(activityPersistence, expressionEvaluator, dispatcher);
        this.variablesService = variablesService;
    }

    @Override
    public ActivityExecution complete(ActivityExecution activity) {
        var nextActivities = getNextActivities(activity);

        if (nextActivities.isEmpty()) {
            throw GatewayException.noValidPath(activity);
        }

        return activityPersistence.complete(activity);
    }

    @Override
    public List<ActivityDefinition> getNextActivities(ActivityExecution activity) {
        var variables = variablesService.getScopedVariables(activity);
        var inclusiveGateway = (InclusiveGateway) activity.definition();
        var nextActivityIds = evaluateInclusiveConditions(inclusiveGateway.condition(), variables);
        return getNextActivities(activity.processDefinition(), nextActivityIds);
    }

    @Override
    public ActivityType type() {
        return ActivityType.INCLUSIVE_GATEWAY;
    }

}
