package com.leorces.engine.activity.behaviour.event.end;

import com.leorces.api.exception.ExecutionException;
import com.leorces.engine.activity.behaviour.AbstractActivityBehavior;
import com.leorces.engine.activity.command.TerminateActivityCommand;
import com.leorces.engine.activity.command.TriggerActivityCommand;
import com.leorces.engine.core.CommandDispatcher;
import com.leorces.engine.process.command.IncidentProcessCommand;
import com.leorces.engine.service.resolver.ErrorHandlerResolver;
import com.leorces.model.definition.activity.ActivityDefinition;
import com.leorces.model.definition.activity.ActivityType;
import com.leorces.model.definition.activity.event.end.ErrorEndEvent;
import com.leorces.model.runtime.activity.ActivityExecution;
import com.leorces.model.runtime.process.Process;
import com.leorces.persistence.ActivityPersistence;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@Component
public class ErrorEndEventBehavior extends AbstractActivityBehavior {

    private final ErrorHandlerResolver errorHandlerResolver;

    protected ErrorEndEventBehavior(ActivityPersistence activityPersistence,
                                    CommandDispatcher dispatcher,
                                    ErrorHandlerResolver errorHandlerResolver) {
        super(activityPersistence, dispatcher);
        this.errorHandlerResolver = errorHandlerResolver;
    }

    @Override
    public void complete(ActivityExecution errorEndEvent, Map<String, Object> variables) {
        var completedErrorEndEvent = activityPersistence.complete(errorEndEvent);
        var errorCode = extractErrorCode(completedErrorEndEvent);
        var process = completedErrorEndEvent.process();

        if (correlateInLocalScopes(errorCode, completedErrorEndEvent)) {
            return;
        }

        if (process.isRootProcess()) {
            dispatcher.dispatchAsync(IncidentProcessCommand.of(process.id()));
            return;
        }

        dispatcher.dispatch(TerminateActivityCommand.of(process.id(), true));
        correlateInParentProcesses(errorCode, process);
    }

    @Override
    public List<ActivityDefinition> getNextActivities(ActivityExecution errorEndEvent) {
        return List.of();
    }

    @Override
    public ActivityType type() {
        return ActivityType.ERROR_END_EVENT;
    }

    private boolean correlateInLocalScopes(String errorCode, ActivityExecution errorSource) {
        var process = errorSource.process();

        for (var scopeId : errorSource.scope()) {
            if (correlateInScope(errorCode, scopeId, process)) {
                return true;
            }

            if (isProcessLevelScope(scopeId, process)) {
                return false;
            }

            var definition = getActivityDefinition(scopeId, process);
            if (definition.type().isSubprocess()) {
                dispatcher.dispatch(TerminateActivityCommand.of(process.id(), scopeId, true));
            }
        }

        return false;
    }

    private void correlateInParentProcesses(String errorCode, Process process) {
        var current = process;

        while (current.isCallActivity()) {
            var callActivity = findCallActivity(current.id());
            if (correlateInLocalScopes(errorCode, callActivity)) {
                return;
            }

            current = terminateAndMoveToParent(callActivity, current);
        }
    }

    private Process terminateAndMoveToParent(ActivityExecution callActivity, Process process) {
        if (process.isRootProcess()) {
            dispatcher.dispatchAsync(IncidentProcessCommand.of(process.id()));
            return process;
        }

        dispatcher.dispatch(TerminateActivityCommand.of(callActivity.id(), true));
        return callActivity.process();
    }

    private boolean correlateInScope(String errorCode, String scopeId, Process process) {
        var handlerOpt = errorHandlerResolver.resolve(errorCode, scopeId, process);
        if (handlerOpt.isEmpty()) {
            return false;
        }

        dispatcher.dispatchAsync(TriggerActivityCommand.of(process, handlerOpt.get()));
        return true;
    }

    private ActivityExecution findCallActivity(String callActivityId) {
        return activityPersistence.findById(callActivityId)
                .orElseThrow(() -> ExecutionException.of("Call activity not found", "Call activity with id: %s not found".formatted(callActivityId)));
    }

    private ActivityDefinition getActivityDefinition(String definitionId, Process process) {
        return process.definition().getActivityById(definitionId)
                .orElseThrow(() -> ExecutionException.of("Activity definition not found", "Activity definition not found for definitionId: %s in process: %s".formatted(definitionId, process.id()), process));
    }

    private String extractErrorCode(ActivityExecution activity) {
        return ((ErrorEndEvent) activity.definition()).errorCode();
    }

    private boolean isProcessLevelScope(String scope, Process process) {
        return Objects.equals(scope, process.definitionId());
    }

}
