package com.leorces.engine.activity.behaviour.gateway;

import com.leorces.api.exception.ExecutionException;
import com.leorces.engine.activity.behaviour.AbstractConditionalGatewayBehavior;
import com.leorces.engine.core.CommandDispatcher;
import com.leorces.engine.variables.command.GetScopedVariablesCommand;
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

    protected ExclusiveGatewayBehavior(ExpressionEvaluator expressionEvaluator,
                                       ActivityPersistence activityPersistence,
                                       CommandDispatcher dispatcher) {
        super(activityPersistence, expressionEvaluator, dispatcher);
    }

    @Override
    public void complete(ActivityExecution exclusiveGateway, Map<String, Object> variables) {
        var nextActivities = getNextActivities(exclusiveGateway);

        if (nextActivities.size() != 1) {
            throw ExecutionException.of("No valid path", exclusiveGateway);
        }

        var completedExclusiveGateway = activityPersistence.complete(exclusiveGateway);
        handleActivityCompletion(completedExclusiveGateway, nextActivities);
    }

    @Override
    public List<ActivityDefinition> getNextActivities(ActivityExecution exclusiveGateway) {
        var variables = dispatcher.execute(GetScopedVariablesCommand.of(exclusiveGateway));
        var condition = ((ExclusiveGateway) exclusiveGateway.definition()).condition();
        var nextActivityIds = evaluateExclusiveConditions(condition, variables);
        return getNextActivities(exclusiveGateway.processDefinition(), nextActivityIds);
    }

    @Override
    public ActivityType type() {
        return ActivityType.EXCLUSIVE_GATEWAY;
    }

}
