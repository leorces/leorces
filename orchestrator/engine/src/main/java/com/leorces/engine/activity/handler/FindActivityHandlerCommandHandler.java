package com.leorces.engine.activity.handler;

import com.leorces.api.exception.ExecutionException;
import com.leorces.engine.activity.command.FindActivityHandlerCommand;
import com.leorces.engine.activity.command.FindActivityHandlerCommand.ExecutionResultType;
import com.leorces.engine.core.ResultCommandHandler;
import com.leorces.model.definition.activity.*;
import com.leorces.model.runtime.process.Process;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

@Component
@RequiredArgsConstructor
public class FindActivityHandlerCommandHandler implements ResultCommandHandler<FindActivityHandlerCommand, Optional<ActivityDefinition>> {

    @Override
    public Optional<ActivityDefinition> execute(FindActivityHandlerCommand command) {
        if (isProcessLevelScope(command.scope(), command.process())) {
            return findProcessLevelHandler(command);
        }
        return findScopedHandler(command);
    }

    @Override
    public Class<FindActivityHandlerCommand> getCommandType() {
        return FindActivityHandlerCommand.class;
    }

    private Optional<ActivityDefinition> findProcessLevelHandler(FindActivityHandlerCommand command) {
        return findProcessLevelStartEvent(command.code(), command.process(), command.type())
                .or(() -> findProcessLevelStartEvent(null, command.process(), command.type()));
    }

    private Optional<ActivityDefinition> findScopedHandler(FindActivityHandlerCommand command) {
        return findSpecificHandler(command)
                .or(() -> findCommonHandler(command));
    }

    private Optional<ActivityDefinition> findSpecificHandler(FindActivityHandlerCommand command) {
        return findBoundaryEventInScope(command.code(), command.scope(), command.process(), command.type())
                .or(() -> findStartEventInScope(command.code(), command.scope(), command.process(), command.type()));
    }

    private Optional<ActivityDefinition> findCommonHandler(FindActivityHandlerCommand command) {
        return findBoundaryEventInScope(null, command.scope(), command.process(), command.type())
                .or(() -> findStartEventInScope(null, command.scope(), command.process(), command.type()));
    }

    private Optional<ActivityDefinition> findProcessLevelStartEvent(String code, Process process, ExecutionResultType type) {
        return findAllStartEvents(process, type)
                .filter(startEvent -> matchesCode(startEvent, code, type))
                .findFirst();
    }

    private Optional<ActivityDefinition> findBoundaryEventInScope(String code, String scope, Process process, ExecutionResultType type) {
        return findAllBoundaryEvents(process, type)
                .filter(boundaryEvent -> matchesScope(boundaryEvent, scope))
                .filter(boundaryEvent -> matchesCode(boundaryEvent, code, type))
                .findFirst();
    }

    private Optional<ActivityDefinition> findStartEventInScope(String code, String scope, Process process, ExecutionResultType type) {
        return findAllStartEvents(process, type)
                .filter(startEvent -> isStartEventWithinScope(startEvent, scope, process))
                .filter(startEvent -> matchesCode(startEvent, code, type))
                .findFirst();
    }

    private Stream<ActivityDefinition> findAllBoundaryEvents(Process process, ExecutionResultType type) {
        var eventType = type == ExecutionResultType.ERROR ? ActivityType.ERROR_BOUNDARY_EVENT : ActivityType.ESCALATION_BOUNDARY_EVENT;
        return findActivitiesByType(process, eventType);
    }

    private Stream<ActivityDefinition> findAllStartEvents(Process process, ExecutionResultType type) {
        var eventType = type == ExecutionResultType.ERROR ? ActivityType.ERROR_START_EVENT : ActivityType.ESCALATION_START_EVENT;
        return findActivitiesByType(process, eventType);
    }

    private Stream<ActivityDefinition> findActivitiesByType(Process process, ActivityType type) {
        return process.definition().activities().stream()
                .filter(a -> type.equals(a.type()));
    }

    private boolean matchesScope(ActivityDefinition boundaryEvent, String scope) {
        return Objects.equals(((BoundaryEventDefinition) boundaryEvent).attachedToRef(), scope);
    }

    private boolean matchesCode(ActivityDefinition activity, String code, ExecutionResultType type) {
        var activityCode = type == ExecutionResultType.ERROR
                ? ((ErrorActivityDefinition) activity).errorCode()
                : ((EscalationActivityDefinition) activity).escalationCode();
        return Objects.equals(code, activityCode);
    }

    private boolean isStartEventWithinScope(ActivityDefinition startEvent, String scope, Process process) {
        var parentId = startEvent.parentId();
        var eventSubprocess = process.definition().getActivityById(parentId)
                .orElseThrow(() -> ExecutionException.of("Activity not found", "Activity not found for id: %s".formatted(parentId), process));
        return Objects.equals(eventSubprocess.parentId(), scope);
    }

    private boolean isProcessLevelScope(String scope, Process process) {
        return Objects.equals(scope, process.definitionId());
    }

}
