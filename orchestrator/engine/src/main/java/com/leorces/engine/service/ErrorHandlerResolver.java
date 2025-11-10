package com.leorces.engine.service;

import com.leorces.engine.exception.activity.ActivityNotFoundException;
import com.leorces.model.definition.activity.ActivityDefinition;
import com.leorces.model.definition.activity.ActivityType;
import com.leorces.model.definition.activity.ErrorActivityDefinition;
import com.leorces.model.definition.activity.event.boundary.ErrorBoundaryEvent;
import com.leorces.model.definition.activity.event.start.ErrorStartEvent;
import com.leorces.model.runtime.process.Process;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

@Component
public class ErrorHandlerResolver {

    public Optional<ErrorActivityDefinition> resolve(String errorCode, String scope, Process process) {
        if (isProcessLevelScope(scope, process)) {
            return findProcessLevelErrorHandler(errorCode, process);
        }

        return findScopedErrorHandler(errorCode, scope, process);
    }

    private Optional<ErrorActivityDefinition> findProcessLevelErrorHandler(String errorCode, Process process) {
        return findProcessLevelStartEvent(errorCode, process)
                .or(() -> findProcessLevelStartEvent(null, process));
    }

    private Optional<ErrorActivityDefinition> findScopedErrorHandler(String errorCode, String scope, Process process) {
        return findSpecificErrorHandler(errorCode, scope, process)
                .or(() -> findCommonErrorHandler(scope, process));
    }

    private Optional<ErrorActivityDefinition> findSpecificErrorHandler(String errorCode, String scope, Process process) {
        return findBoundaryEventInScope(errorCode, scope, process)
                .or(() -> findStartEventInScope(errorCode, scope, process));
    }

    private Optional<ErrorActivityDefinition> findCommonErrorHandler(String scope, Process process) {
        return findBoundaryEventInScope(null, scope, process)
                .or(() -> findStartEventInScope(null, scope, process));
    }

    private Optional<ErrorActivityDefinition> findBoundaryEventInScope(String errorCode, String scope, Process process) {
        return findAllBoundaryEvents(process)
                .filter(boundaryEvent -> matchesScope(boundaryEvent, scope))
                .filter(boundaryEvent -> matchesErrorCode(boundaryEvent, errorCode))
                .map(this::castToErrorActivityDefinition)
                .findFirst();
    }

    private Optional<ErrorActivityDefinition> findStartEventInScope(String errorCode, String scope, Process process) {
        return findAllStartEvents(process)
                .filter(startEvent -> isStartEventWithinScope(startEvent, scope, process))
                .filter(startEvent -> matchesErrorCode(startEvent, errorCode))
                .map(this::castToErrorActivityDefinition)
                .findFirst();
    }

    private Optional<ErrorActivityDefinition> findProcessLevelStartEvent(String errorCode, Process process) {
        return findAllStartEvents(process)
                .filter(startEvent -> matchesErrorCode(startEvent, errorCode))
                .map(this::castToErrorActivityDefinition)
                .findFirst();
    }

    private Stream<ErrorBoundaryEvent> findAllBoundaryEvents(Process process) {
        return findActivitiesByType(process, ActivityType.ERROR_BOUNDARY_EVENT)
                .map(activity -> (ErrorBoundaryEvent) activity);
    }

    private Stream<ErrorStartEvent> findAllStartEvents(Process process) {
        return findActivitiesByType(process, ActivityType.ERROR_START_EVENT)
                .map(activity -> (ErrorStartEvent) activity);
    }

    private Stream<?> findActivitiesByType(Process process, ActivityType activityType) {
        return process.definition().activities().stream()
                .filter(activity -> activityType.equals(activity.type()));
    }

    private boolean matchesScope(ErrorBoundaryEvent boundaryEvent, String scope) {
        return boundaryEvent.attachedToRef().equals(scope);
    }

    private boolean matchesErrorCode(ErrorActivityDefinition errorActivity, String errorCode) {
        return Objects.equals(errorCode, errorActivity.errorCode());
    }

    private ErrorActivityDefinition castToErrorActivityDefinition(ErrorActivityDefinition errorActivity) {
        return errorActivity;
    }

    private boolean isStartEventWithinScope(ErrorStartEvent startEvent, String scope, Process process) {
        var eventSubprocess = findActivityById(startEvent.parentId(), process);
        return Objects.equals(eventSubprocess.parentId(), scope);
    }

    private ActivityDefinition findActivityById(String activityId, Process process) {
        return process.definition().getActivityById(activityId)
                .orElseThrow(() -> ActivityNotFoundException.activityDefinitionNotFound(activityId, process.id()));
    }

    private boolean isProcessLevelScope(String scope, Process process) {
        return Objects.equals(scope, process.definitionId());
    }

}