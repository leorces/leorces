package com.leorces.engine.activity.behaviour.gateway;

import com.leorces.engine.activity.behaviour.FailableActivityBehavior;
import com.leorces.engine.event.EngineEventBus;
import com.leorces.engine.event.activity.ActivityEvent;
import com.leorces.juel.ExpressionEvaluator;
import com.leorces.model.definition.ProcessDefinition;
import com.leorces.model.definition.activity.ActivityDefinition;
import com.leorces.model.runtime.activity.ActivityExecution;
import com.leorces.persistence.ActivityPersistence;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public abstract class AbstractGatewayBehavior implements FailableActivityBehavior {

    protected final ActivityPersistence activityPersistence;
    protected final ExpressionEvaluator expressionEvaluator;
    protected final EngineEventBus eventBus;

    protected AbstractGatewayBehavior(ActivityPersistence activityPersistence,
                                      ExpressionEvaluator expressionEvaluator,
                                      EngineEventBus eventBus) {
        this.activityPersistence = activityPersistence;
        this.expressionEvaluator = expressionEvaluator;
        this.eventBus = eventBus;
    }

    @Override
    public void fail(ActivityExecution activity) {
        activityPersistence.fail(activity);
        eventBus.publish(ActivityEvent.incidentFailEvent(activity));
    }

    @Override
    public void retry(ActivityExecution activity) {
        eventBus.publish(ActivityEvent.runAsync(activity));
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
