package com.leorces.engine.activity.behaviour;

import com.leorces.engine.activity.command.TriggerActivityCommand;
import com.leorces.engine.core.CommandDispatcher;
import com.leorces.engine.exception.activity.ActivityNotFoundException;
import com.leorces.engine.service.resolver.EscalationHandlerResolver;
import com.leorces.engine.variables.command.SetActivityVariablesCommand;
import com.leorces.model.definition.activity.EscalationActivityDefinition;
import com.leorces.model.definition.activity.event.boundary.EscalationBoundaryEvent;
import com.leorces.model.definition.activity.event.start.EscalationStartEvent;
import com.leorces.model.runtime.activity.ActivityExecution;
import com.leorces.model.runtime.process.Process;
import com.leorces.persistence.ActivityPersistence;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Map;

public abstract class AbstractThrowEscalationBehavior extends AbstractActivityBehavior {

    private final EscalationHandlerResolver escalationHandlerResolver;

    protected AbstractThrowEscalationBehavior(ActivityPersistence activityPersistence,
                                              CommandDispatcher dispatcher,
                                              EscalationHandlerResolver escalationHandlerResolver) {
        super(activityPersistence, dispatcher);
        this.escalationHandlerResolver = escalationHandlerResolver;
    }

    @Override
    public void complete(ActivityExecution activity, Map<String, Object> variables) {
        var completedEndEvent = activityPersistence.complete(activity);
        setVariables(completedEndEvent, variables);

        var handlerProcessPair = resolveHandler(
                extractEscalationCode(completedEndEvent),
                completedEndEvent,
                completedEndEvent.process()
        );

        if (handlerProcessPair == null) {
            handleActivityCompletion(completedEndEvent, getNextActivities(completedEndEvent));
            return;
        }

        var handler = handlerProcessPair.getKey();
        var handlerProcess = handlerProcessPair.getValue();

        if (shouldInterrupt(handler)) {
            dispatcher.dispatch(TriggerActivityCommand.of(handlerProcess, handler));
        } else {
            dispatcher.dispatchAsync(TriggerActivityCommand.of(handlerProcess, handler));
            handleActivityCompletion(completedEndEvent, getNextActivities(completedEndEvent));
        }
    }

    private Pair<EscalationActivityDefinition, Process> resolveHandler(String escalationCode,
                                                                       ActivityExecution source,
                                                                       Process process) {
        var currentActivity = source;
        var currentProcess = process;

        while (currentActivity != null) {
            var handler = findInLocalScopes(escalationCode, currentActivity, currentProcess);
            if (handler != null) {
                return Pair.of(handler, currentProcess);
            }

            if (!currentProcess.isCallActivity()) {
                break;
            }

            currentActivity = findParentCallActivity(currentProcess.id());
            currentProcess = currentActivity.process();
        }

        return null;
    }

    private EscalationActivityDefinition findInLocalScopes(String code,
                                                           ActivityExecution activity,
                                                           Process process) {
        for (var scope : activity.scope()) {
            var handler = escalationHandlerResolver.resolve(code, scope, process);
            if (handler.isPresent()) {
                return handler.get();
            }
        }
        return null;
    }

    private ActivityExecution findParentCallActivity(String callActivityId) {
        return activityPersistence.findById(callActivityId)
                .orElseThrow(() -> ActivityNotFoundException.activityNotFoundById(callActivityId));
    }

    private void setVariables(ActivityExecution completedThrowEvent, Map<String, Object> variables) {
        dispatcher.dispatch(SetActivityVariablesCommand.of(completedThrowEvent, variables));
    }

    private boolean shouldInterrupt(EscalationActivityDefinition handler) {
        return switch (handler) {
            case EscalationBoundaryEvent boundary -> boundary.cancelActivity();
            case EscalationStartEvent startEvent -> startEvent.isInterrupting();
            default -> false;
        };
    }

    private String extractEscalationCode(ActivityExecution escalationEvent) {
        return ((EscalationActivityDefinition) escalationEvent.definition()).escalationCode();
    }

}
