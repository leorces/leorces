package com.leorces.engine.activity.behaviour.event.intermediate;

import com.leorces.engine.activity.behaviour.AbstractTriggerableCatchBehavior;
import com.leorces.engine.activity.command.CompleteActivityCommand;
import com.leorces.engine.core.CommandDispatcher;
import com.leorces.engine.service.variable.VariablesService;
import com.leorces.juel.ExpressionEvaluator;
import com.leorces.model.definition.activity.ActivityType;
import com.leorces.model.definition.activity.event.intermediate.IntermediateCatchEvent;
import com.leorces.model.runtime.activity.ActivityExecution;
import com.leorces.persistence.ActivityPersistence;
import org.springframework.stereotype.Component;

@Component
public class IntermediateCatchEventBehavior extends AbstractTriggerableCatchBehavior {

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
    public void run(ActivityExecution intermediateCatchEvent) {
        var newIntermediateCatchEvent = activityPersistence.run(intermediateCatchEvent);

        if (isConditionMatched(newIntermediateCatchEvent)) {
            dispatcher.dispatchAsync(CompleteActivityCommand.of(newIntermediateCatchEvent));
        }
    }

    @Override
    public ActivityType type() {
        return ActivityType.INTERMEDIATE_CATCH_EVENT;
    }

    private boolean isConditionMatched(ActivityExecution intermediateCatchEvent) {
        var variables = variablesService.getScopedVariables(intermediateCatchEvent);
        var definition = (IntermediateCatchEvent) intermediateCatchEvent.definition();
        return expressionEvaluator.evaluateBoolean(definition.condition(), variables);
    }

}
