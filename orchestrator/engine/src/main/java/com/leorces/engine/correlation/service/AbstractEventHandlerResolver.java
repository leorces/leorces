package com.leorces.engine.correlation.service;

import com.leorces.engine.exception.activity.ActivityNotFoundException;
import com.leorces.model.definition.activity.ActivityDefinition;
import com.leorces.model.definition.activity.ActivityType;
import com.leorces.model.runtime.process.Process;

import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;


public abstract class AbstractEventHandlerResolver<
        ACTIVITY_DEFINITION extends ActivityDefinition,
        BOUNDARY_EVENT extends ACTIVITY_DEFINITION,
        START_EVENT extends ACTIVITY_DEFINITION> {

    public Optional<ACTIVITY_DEFINITION> resolve(String code, String scope, Process process) {
        if (isProcessLevelScope(scope, process)) {
            return findProcessLevelHandler(code, process);
        }
        return findScopedHandler(code, scope, process);
    }

    protected abstract ActivityType boundaryEventType();

    protected abstract ActivityType startEventType();

    protected abstract String codeOf(ACTIVITY_DEFINITION definition);

    protected abstract String parentIdOf(START_EVENT startEvent);

    protected abstract String attachedToRefOf(BOUNDARY_EVENT boundaryEvent);

    private Optional<ACTIVITY_DEFINITION> findProcessLevelHandler(String code, Process process) {
        return findProcessLevelStartEvent(code, process)
                .or(() -> findProcessLevelStartEvent(null, process));
    }

    private Optional<ACTIVITY_DEFINITION> findScopedHandler(String code, String scope, Process process) {
        return findSpecificHandler(code, scope, process)
                .or(() -> findCommonHandler(scope, process));
    }

    private Optional<ACTIVITY_DEFINITION> findSpecificHandler(String code, String scope, Process process) {
        return findBoundaryEventInScope(code, scope, process)
                .or(() -> findStartEventInScope(code, scope, process));
    }

    private Optional<ACTIVITY_DEFINITION> findCommonHandler(String scope, Process process) {
        return findBoundaryEventInScope(null, scope, process)
                .or(() -> findStartEventInScope(null, scope, process));
    }

    private Optional<ACTIVITY_DEFINITION> findProcessLevelStartEvent(String code, Process process) {
        return findAllStartEvents(process)
                .filter(startEvent -> matchesCode(startEvent, code))
                .map(this::cast)
                .findFirst();
    }

    private Optional<ACTIVITY_DEFINITION> findBoundaryEventInScope(String code, String scope, Process process) {
        return findAllBoundaryEvents(process)
                .filter(boundaryEvent -> matchesScope(boundaryEvent, scope))
                .filter(boundaryEvent -> matchesCode(boundaryEvent, code))
                .map(this::cast)
                .findFirst();
    }

    private Optional<ACTIVITY_DEFINITION> findStartEventInScope(String code, String scope, Process process) {
        return findAllStartEvents(process)
                .filter(startEvent -> isStartEventWithinScope(startEvent, scope, process))
                .filter(startEvent -> matchesCode(startEvent, code))
                .map(this::cast)
                .findFirst();
    }

    private Stream<BOUNDARY_EVENT> findAllBoundaryEvents(Process process) {
        return findActivitiesByType(process, boundaryEventType())
                .map(activity -> (BOUNDARY_EVENT) activity);
    }

    private Stream<START_EVENT> findAllStartEvents(Process process) {
        return findActivitiesByType(process, startEventType())
                .map(activity -> (START_EVENT) activity);
    }

    private Stream<ActivityDefinition> findActivitiesByType(Process process, ActivityType type) {
        return process.definition().activities().stream()
                .filter(a -> type.equals(a.type()));
    }

    private boolean matchesScope(BOUNDARY_EVENT boundaryEvent, String scope) {
        return Objects.equals(attachedToRefOf(boundaryEvent), scope);
    }

    private boolean matchesCode(ACTIVITY_DEFINITION activity, String code) {
        return Objects.equals(code, codeOf(activity));
    }

    private boolean isStartEventWithinScope(START_EVENT startEvent, String scope, Process process) {
        var eventSubprocess = findActivityById(parentIdOf(startEvent), process);
        return Objects.equals(eventSubprocess.parentId(), scope);
    }

    private ActivityDefinition findActivityById(String id, Process process) {
        return process.definition().getActivityById(id)
                .orElseThrow(() -> ActivityNotFoundException.activityDefinitionNotFound(id, process.id()));
    }

    private boolean isProcessLevelScope(String scope, Process process) {
        return Objects.equals(scope, process.definitionId());
    }

    private ACTIVITY_DEFINITION cast(ACTIVITY_DEFINITION def) {
        return def;
    }

}
