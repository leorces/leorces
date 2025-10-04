package com.leorces.engine.activity.behaviour.event;

import com.leorces.engine.activity.behaviour.AbstractActivityBehavior;
import com.leorces.engine.activity.behaviour.TriggerableActivityBehaviour;
import com.leorces.engine.activity.command.CompleteActivityCommand;
import com.leorces.engine.core.CommandDispatcher;
import com.leorces.engine.variables.VariablesService;
import com.leorces.juel.ExpressionEvaluator;
import com.leorces.model.definition.activity.ActivityDefinition;
import com.leorces.model.definition.activity.ActivityType;
import com.leorces.model.definition.activity.event.IntermediateCatchEvent;
import com.leorces.model.runtime.activity.ActivityExecution;
import com.leorces.model.runtime.process.Process;
import com.leorces.persistence.ActivityPersistence;
import org.springframework.stereotype.Component;

@Component
public class IntermediateCatchEventBehavior extends AbstractActivityBehavior implements TriggerableActivityBehaviour {

    private final VariablesService variablesService;
    private final ExpressionEvaluator expressionEvaluator;

    protected IntermediateCatchEventBehavior(ActivityPersistence activityPersistence,
                                             CommandDispatcher dispatcher,
                                             VariablesService variablesService,
                                             ExpressionEvaluator expressionEvaluator) {
        super(activityPersistence, dispatcher);
        this.variablesService = variablesService;
        this.expressionEvaluator = expressionEvaluator;
    }

    @Override
    public void trigger(Process process, ActivityDefinition definition) {
        activityPersistence.findByDefinitionId(process.id(), definition.id())
                .ifPresent(activity -> dispatcher.dispatchAsync(CompleteActivityCommand.of(activity)));
    }

    @Override
    public void run(ActivityExecution activity) {
        var result = activityPersistence.run(activity);

        if (isConditionMatched(result)) {
            dispatcher.dispatchAsync(CompleteActivityCommand.of(result));
        }
    }

    @Override
    public ActivityExecution complete(ActivityExecution activity) {
        var result = activityPersistence.complete(activity);
        completeEventBasedGatewayActivities(result);
        return result;
    }

    @Override
    public ActivityExecution terminate(ActivityExecution activity) {
        var result = activityPersistence.terminate(activity);
        completeEventBasedGatewayActivities(result);
        return result;
    }

    @Override
    public ActivityType type() {
        return ActivityType.INTERMEDIATE_CATCH_EVENT;
    }

    private boolean isConditionMatched(ActivityExecution activity) {
        var variables = variablesService.getScopedVariables(activity);
        var definition = (IntermediateCatchEvent) activity.definition();
        return expressionEvaluator.evaluateBoolean(definition.condition(), variables);
    }

}
