package com.leorces.engine.correlation.handler;

import com.leorces.engine.activity.command.HandleActivityCompletionWithoutNextActivitiesCommand;
import com.leorces.engine.activity.command.TriggerActivityCommand;
import com.leorces.engine.core.CommandDispatcher;
import com.leorces.engine.core.CommandHandler;
import com.leorces.engine.correlation.command.CorrelateEscalationCommand;
import com.leorces.engine.correlation.service.EscalationHandlerResolver;
import com.leorces.engine.exception.activity.ActivityNotFoundException;
import com.leorces.model.definition.activity.EscalationActivityDefinition;
import com.leorces.model.definition.activity.event.boundary.EscalationBoundaryEvent;
import com.leorces.model.definition.activity.event.start.EscalationStartEvent;
import com.leorces.model.runtime.activity.ActivityExecution;
import com.leorces.model.runtime.process.Process;
import com.leorces.persistence.ActivityPersistence;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CorrelateEscalationCommandHandler implements CommandHandler<CorrelateEscalationCommand> {

    private final ActivityPersistence activityPersistence;
    private final EscalationHandlerResolver escalationHandlerResolver;
    private final CommandDispatcher dispatcher;

    @Override
    public void handle(CorrelateEscalationCommand command) {
        var escalationEvent = command.escalationEvent();
        var process = escalationEvent.process();
        var escalationCode = extractEscalationCode(escalationEvent);

        var result = resolveHandler(escalationCode, escalationEvent, process);
        triggerEscalationHandler(escalationEvent, result);
    }

    @Override
    public Class<CorrelateEscalationCommand> getCommandType() {
        return CorrelateEscalationCommand.class;
    }

    private void triggerEscalationHandler(ActivityExecution escalationEvent,
                                          Pair<EscalationActivityDefinition, Process> handlerProcessPair) {

        if (handlerProcessPair == null) {
            dispatcher.dispatch(HandleActivityCompletionWithoutNextActivitiesCommand.of(escalationEvent));
            return;
        }

        var process = handlerProcessPair.getRight();
        var handler = handlerProcessPair.getLeft();

        if (handler instanceof EscalationBoundaryEvent boundary) {
            handleBoundaryEscalation(process, escalationEvent, boundary);
        } else if (handler instanceof EscalationStartEvent startEvent) {
            handleEscalation(process, escalationEvent, startEvent);
        }
    }

    private void handleBoundaryEscalation(Process process,
                                          ActivityExecution escalationEvent,
                                          EscalationBoundaryEvent boundary) {
        if (boundary.cancelActivity()) {
            dispatcher.dispatch(TriggerActivityCommand.of(process, boundary));
        } else {
            triggerAndCompleteEscalationEvent(process, escalationEvent, boundary);
        }
    }

    private void handleEscalation(Process process,
                                  ActivityExecution escalationEvent,
                                  EscalationStartEvent startEvent) {
        if (startEvent.isInterrupting()) {
            dispatcher.dispatch(TriggerActivityCommand.of(process, startEvent));
        } else {
            triggerAndCompleteEscalationEvent(process, escalationEvent, startEvent);
        }
    }

    private void triggerAndCompleteEscalationEvent(Process process,
                                                   ActivityExecution escalationEvent,
                                                   EscalationActivityDefinition handler) {
        dispatcher.dispatchAsync(TriggerActivityCommand.of(process, handler));
        dispatcher.dispatch(HandleActivityCompletionWithoutNextActivitiesCommand.of(escalationEvent));
    }

    private Pair<EscalationActivityDefinition, Process> resolveHandler(String code,
                                                                       ActivityExecution source,
                                                                       Process process) {
        var currentActivity = source;
        var currentProcess = process;

        while (currentActivity != null) {
            var handler = findInLocalScopes(code, currentActivity, currentProcess);
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

    private String extractEscalationCode(ActivityExecution escalationEvent) {
        var definition = (EscalationActivityDefinition) escalationEvent.definition();
        return definition.escalationCode();
    }

}
