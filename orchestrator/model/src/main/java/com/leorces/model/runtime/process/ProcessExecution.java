package com.leorces.model.runtime.process;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.leorces.model.definition.ProcessDefinition;
import com.leorces.model.runtime.activity.Activity;
import com.leorces.model.runtime.variable.Variable;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

@Builder(toBuilder = true)
public record ProcessExecution(
        String id,
        String rootProcessId,
        String parentId,
        String businessKey,
        List<Variable> variables,
        List<Activity> activities,
        ProcessState state,
        ProcessDefinition definition,
        boolean suspended,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        LocalDateTime startedAt,
        LocalDateTime completedAt
) {

    public boolean isActive() {
        return state == ProcessState.ACTIVE;
    }

    public boolean isCompleted() {
        return state == ProcessState.COMPLETED;
    }

    public boolean isTerminated() {
        return state == ProcessState.TERMINATED;
    }

    public boolean isIncident() {
        return state == ProcessState.INCIDENT;
    }

    public boolean isInTerminalState() {
        return state == ProcessState.TERMINATED
                || state == ProcessState.COMPLETED
                || state == ProcessState.DELETED;
    }

    // It's possible to have multiple activities with the same definition id as it can be run more than once
    @JsonIgnore
    public List<Activity> getActivitiesByDefinitionId(String definitionId) {
        return activities.stream()
                .filter(activity -> activity.definitionId().equals(definitionId))
                .toList();
    }

}
