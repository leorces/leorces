package com.leorces.engine.correlation.handler;

import com.leorces.engine.activity.command.TerminateActivityCommand;
import com.leorces.engine.activity.command.TriggerActivityCommand;
import com.leorces.engine.core.CommandDispatcher;
import com.leorces.engine.core.CommandHandler;
import com.leorces.engine.correlation.command.CorrelateErrorCommand;
import com.leorces.engine.exception.activity.ActivityNotFoundException;
import com.leorces.engine.process.command.IncidentProcessCommand;
import com.leorces.engine.service.ErrorHandlerResolver;
import com.leorces.model.definition.activity.ActivityDefinition;
import com.leorces.model.definition.activity.event.end.ErrorEndEvent;
import com.leorces.model.runtime.activity.ActivityExecution;
import com.leorces.model.runtime.process.Process;
import com.leorces.persistence.ActivityPersistence;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Slf4j
@Component
@RequiredArgsConstructor
public class CorrelateErrorCommandHandler implements CommandHandler<CorrelateErrorCommand> {

    private final ActivityPersistence activityPersistence;
    private final ErrorHandlerResolver errorHandlerResolver;
    private final CommandDispatcher dispatcher;

    @Override
    public void handle(CorrelateErrorCommand command) {
        var errorEndActivity = command.activity();
        var errorCode = extractErrorCode(errorEndActivity);
        var process = errorEndActivity.process();

        // Correlate inside the current process
        var isCorrelated = correlate(errorCode, errorEndActivity);

        if (isCorrelated) {
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
    public Class<CorrelateErrorCommand> getCommandType() {
        return CorrelateErrorCommand.class;
    }

    private void correlateInParentProcesses(String errorCode, Process currentProcess) {
        while (currentProcess.isCallActivity()) {
            var callActivity = findCallActivity(currentProcess.id());

            if (correlate(errorCode, callActivity)) {
                return;
            }

            currentProcess = correlateInProcess(callActivity, currentProcess);
        }
    }

    private Process correlateInProcess(ActivityExecution callActivity, Process process) {
        if (process.isRootProcess()) {
            dispatcher.dispatchAsync(IncidentProcessCommand.of(process.id()));
            return process;
        }

        dispatcher.dispatch(TerminateActivityCommand.of(callActivity.id(), true));
        return callActivity.process();
    }

    private boolean correlate(String errorCode, ActivityExecution errorSourceActivity) {
        var process = errorSourceActivity.process();
        var scope = errorSourceActivity.scope();

        for (var singleScope : scope) {
            if (correlateInScope(errorCode, singleScope, process)) {
                return true;
            }

            if (Objects.equals(singleScope, process.definitionId())) {
                continue;
            }

            var activityDefinition = getActivityDefinition(singleScope, process);
            if (activityDefinition.type().isSubprocess()) {
                dispatcher.dispatch(TerminateActivityCommand.of(process.id(), singleScope, true));
            }
        }

        return false;
    }

    private boolean correlateInScope(String errorCode, String singleScope, Process process) {
        var errorEventHandler = errorHandlerResolver.resolve(errorCode, singleScope, process);
        if (errorEventHandler.isEmpty()) {
            return false;
        }

        dispatcher.dispatchAsync(TriggerActivityCommand.of(process, errorEventHandler.get()));
        return true;
    }

    private ActivityExecution findCallActivity(String callActivityId) {
        return activityPersistence.findById(callActivityId).orElseThrow();
    }

    private ActivityDefinition getActivityDefinition(String definitionId, Process process) {
        return process.definition().getActivityById(definitionId)
                .orElseThrow(() -> ActivityNotFoundException.activityDefinitionNotFound(definitionId, process.id()));
    }

    private String extractErrorCode(ActivityExecution errorEndActivity) {
        var definition = (ErrorEndEvent) errorEndActivity.definition();
        return definition.errorCode();
    }

}