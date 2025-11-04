package com.leorces.engine.activity.behaviour.gateway;

import com.leorces.engine.activity.behaviour.ActivityCompletionResult;
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
    public ActivityCompletionResult complete(ActivityExecution activity) {
        var nextActivities = getNextActivities(activity);

        if (nextActivities.size() != 1) {
            throw GatewayException.noValidPath(activity);
        }

        var completedActivity = activityPersistence.complete(activity);
        return ActivityCompletionResult.completed(completedActivity, nextActivities);
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
