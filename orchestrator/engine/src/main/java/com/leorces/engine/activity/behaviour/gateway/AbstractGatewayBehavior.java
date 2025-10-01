package com.leorces.engine.activity.behaviour.gateway;

import com.leorces.engine.activity.behaviour.AbstractActivityBehavior;
import com.leorces.engine.core.CommandDispatcher;
import com.leorces.juel.ExpressionEvaluator;
import com.leorces.model.definition.ProcessDefinition;
import com.leorces.model.definition.activity.ActivityDefinition;
import com.leorces.persistence.ActivityPersistence;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public abstract class AbstractGatewayBehavior extends AbstractActivityBehavior {

    protected final ExpressionEvaluator expressionEvaluator;

    protected AbstractGatewayBehavior(ActivityPersistence activityPersistence, ExpressionEvaluator expressionEvaluator, CommandDispatcher dispatcher) {
        super(activityPersistence, dispatcher);
        this.expressionEvaluator = expressionEvaluator;
    }

    protected List<ActivityDefinition> getNextActivities(ProcessDefinition processDefinition, List<String> activityIds) {
        return activityIds.stream()
                .map(processDefinition::getActivityById)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();
    }

    protected List<String> evaluateInclusiveConditions(Map<String, List<String>> conditions, Map<String, Object> variables) {
        var matchedIds = conditions.entrySet().stream()
                .filter(conditionEntry -> !conditionEntry.getKey().isEmpty())
                .filter(conditionEntry -> expressionEvaluator.evaluateBoolean(conditionEntry.getKey(), variables))
                .map(Map.Entry::getValue)
                .flatMap(Collection::stream)
                .toList();

        return matchedIds.isEmpty()
                ? getDefaultInclusiveActivityIds(conditions)
                : matchedIds;
    }

    protected List<String> evaluateExclusiveConditions(Map<String, String> conditions, Map<String, Object> variables) {
        var matchedIds = conditions.entrySet().stream()
                .filter(conditionEntry -> !conditionEntry.getKey().isEmpty())
                .filter(conditionEntry -> expressionEvaluator.evaluateBoolean(conditionEntry.getKey(), variables))
                .map(Map.Entry::getValue)
                .toList();

        return matchedIds.isEmpty()
                ? getDefaultExclusiveActivityIds(conditions)
                : matchedIds;
    }

    protected List<String> getDefaultInclusiveActivityIds(Map<String, List<String>> conditions) {
        return conditions.entrySet().stream()
                .filter(conditionEntry -> conditionEntry.getKey().isEmpty())
                .map(Map.Entry::getValue)
                .flatMap(Collection::stream)
                .toList();
    }

    protected List<String> getDefaultExclusiveActivityIds(Map<String, String> conditions) {
        return conditions.entrySet().stream()
                .filter(conditionEntry -> conditionEntry.getKey().isEmpty())
                .map(Map.Entry::getValue)
                .toList();
    }

}
