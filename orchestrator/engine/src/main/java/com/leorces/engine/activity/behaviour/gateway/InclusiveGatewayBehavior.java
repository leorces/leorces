package com.leorces.engine.activity.behaviour.gateway;

import com.leorces.api.exception.ExecutionException;
import com.leorces.engine.activity.behaviour.AbstractConditionalGatewayBehavior;
import com.leorces.engine.core.CommandDispatcher;
import com.leorces.engine.variables.command.GetScopedVariablesCommand;
import com.leorces.juel.ExpressionEvaluator;
import com.leorces.model.definition.activity.ActivityDefinition;
import com.leorces.model.definition.activity.ActivityType;
import com.leorces.model.definition.activity.gateway.InclusiveGateway;
import com.leorces.model.runtime.activity.ActivityExecution;
import com.leorces.persistence.ActivityPersistence;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class InclusiveGatewayBehavior extends AbstractConditionalGatewayBehavior {

    protected InclusiveGatewayBehavior(ExpressionEvaluator expressionEvaluator,
                                       ActivityPersistence activityPersistence,
                                       CommandDispatcher dispatcher) {
        super(activityPersistence, expressionEvaluator, dispatcher);
    }

    @Override
    public void complete(ActivityExecution inclusiveGateway, Map<String, Object> variables) {
        var nextActivities = getNextActivities(inclusiveGateway);

        if (nextActivities.isEmpty()) {
            throw ExecutionException.of("No valid path", inclusiveGateway);
        }

        var completedInclusiveGateway = activityPersistence.complete(inclusiveGateway);
        handleActivityCompletion(completedInclusiveGateway, nextActivities);
    }

    @Override
    public List<ActivityDefinition> getNextActivities(ActivityExecution inclusiveGateway) {
        var variables = dispatcher.execute(GetScopedVariablesCommand.of(inclusiveGateway));
        var condition = ((InclusiveGateway) inclusiveGateway.definition()).condition();
        var nextActivityIds = evaluateInclusiveConditions(condition, variables);
        return getNextActivities(inclusiveGateway.processDefinition(), nextActivityIds);
    }

    @Override
    public ActivityType type() {
        return ActivityType.INCLUSIVE_GATEWAY;
    }

}
