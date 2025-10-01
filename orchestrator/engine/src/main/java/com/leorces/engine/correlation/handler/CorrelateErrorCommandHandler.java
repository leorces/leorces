package com.leorces.engine.correlation.handler;

import com.leorces.engine.activity.command.TerminateActivityCommand;
import com.leorces.engine.activity.command.TriggerActivityCommand;
import com.leorces.engine.core.CommandDispatcher;
import com.leorces.engine.core.CommandHandler;
import com.leorces.engine.correlation.command.CorrelateErrorCommand;
import com.leorces.engine.process.command.IncidentProcessCommand;
import com.leorces.model.definition.activity.ActivityType;
import com.leorces.model.definition.activity.ErrorActivityDefinition;
import com.leorces.model.definition.activity.event.ErrorBoundaryEvent;
import com.leorces.model.definition.activity.event.ErrorEndEvent;
import com.leorces.model.runtime.activity.ActivityExecution;
import com.leorces.model.runtime.process.Process;
import com.leorces.persistence.ActivityPersistence;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class CorrelateErrorCommandHandler implements CommandHandler<CorrelateErrorCommand> {

    private final ActivityPersistence activityPersistence;
    private final CommandDispatcher dispatcher;

    @Override
    public void handle(CorrelateErrorCommand command) {
        var errorEndActivity = command.activity();

        var definition = (ErrorEndEvent) errorEndActivity.definition();
        var errorCode = definition.errorCode();
        var scope = errorEndActivity.scope();
        var process = errorEndActivity.process();
        var errorHandler = findErrorEventHandlerCmd(errorCode, scope, process);

        if (errorHandler.isPresent()) {
            triggerErrorHandler(errorHandler.get(), process);
            return;
        }

        if (process.parentId() == null) {
            dispatcher.dispatchAsync(IncidentProcessCommand.of(process.id()));
            return;
        }

        correlateInParentProcesses(errorCode, errorEndActivity.process());
    }

    @Override
    public Class<CorrelateErrorCommand> getCommandType() {
        return CorrelateErrorCommand.class;
    }

    private void correlateInParentProcesses(String errorCode, Process currentProcess) {
        while (currentProcess.isCallActivity()) {
            var callActivity = findCallActivity(currentProcess.id());
            var scope = callActivity.scope();
            var process = callActivity.process();
            var errorHandler = findErrorEventHandlerCmd(errorCode, scope, process);

            if (errorHandler.isEmpty()) {
                dispatcher.dispatch(TerminateActivityCommand.of(callActivity, true));
                currentProcess = callActivity.process();
                continue;
            }

            if (errorHandler.get().type() == ActivityType.ERROR_START_EVENT) {
                dispatcher.dispatch(TerminateActivityCommand.of(callActivity, true));
            }

            triggerErrorHandler(errorHandler.get(), process);
            return;
        }

        dispatcher.dispatchAsync(IncidentProcessCommand.of(currentProcess.id()));
    }

    private void triggerErrorHandler(ErrorActivityDefinition event, Process process) {
        dispatcher.dispatchAsync(TriggerActivityCommand.of(process, event));
    }

    private Optional<ErrorActivityDefinition> findErrorEventHandlerCmd(String errorCode, List<String> scope, Process process) {
        return findErrorBoundaryEvent(errorCode, scope, process)
                .or(() -> findErrorStartEvent(errorCode, scope, process));
    }

    private ActivityExecution findCallActivity(String callActivityId) {
        return activityPersistence.findById(callActivityId).orElseThrow();
    }

    private Optional<ErrorActivityDefinition> findErrorBoundaryEvent(String errorCode, List<String> scope, Process process) {
        var errorBoundaryEvents = process.definition().activities().stream()
                .filter(def -> def.type() == ActivityType.ERROR_BOUNDARY_EVENT)
                .map(ErrorBoundaryEvent.class::cast)
                .filter(event -> scope.contains(event.attachedToRef()))
                .filter(event -> errorCode.equals(event.errorCode()))
                .toList();

        if (errorBoundaryEvents.isEmpty()) {
            return Optional.empty();
        }

        return scope.stream()
                .map(definitionId -> getBoundaryEventAttachedToActivity(definitionId, errorBoundaryEvents))
                .filter(Objects::nonNull)
                .findFirst();
    }

    private Optional<ErrorActivityDefinition> findErrorStartEvent(String errorCode, List<String> scope, Process process) {
        var errorStartEvents = process.definition().activities().stream()
                .filter(def -> def.type() == ActivityType.ERROR_START_EVENT)
                .map(ErrorActivityDefinition.class::cast)
                .filter(event -> errorCode.equals(event.errorCode()))
                .toList();

        if (errorStartEvents.isEmpty()) {
            return Optional.empty();
        }

        return scope.stream()
                .map(definitionId -> getErrorStartEvent(definitionId, errorStartEvents))
                .filter(Objects::nonNull)
                .findFirst()
                .or(() -> Optional.ofNullable(errorStartEvents.getFirst()));
    }

    private ErrorActivityDefinition getBoundaryEventAttachedToActivity(String definitionId, List<ErrorBoundaryEvent> events) {
        return events.stream()
                .filter(event -> event.attachedToRef().equals(definitionId))
                .findFirst()
                .orElse(null);
    }

    private ErrorActivityDefinition getErrorStartEvent(String definitionId, List<ErrorActivityDefinition> events) {
        return events.stream()
                .filter(event -> event.parentId().equals(definitionId))
                .findFirst()
                .orElse(null);
    }

}
