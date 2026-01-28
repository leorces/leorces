package com.leorces.model.runtime.process;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.leorces.model.definition.ProcessDefinition;
import com.leorces.model.runtime.variable.Variable;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Builder(toBuilder = true)
public record Process(
        String id,
        String rootProcessId,
        String parentId,
        String businessKey,
        List<Variable> variables,
        ProcessState state,
        ProcessDefinition definition,
        boolean suspended,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        LocalDateTime startedAt,
        LocalDateTime completedAt
) {

    public List<Variable> variables() {
        return Objects.requireNonNullElseGet(variables, List::of);
    }

    @JsonIgnore
    public String definitionId() {
        return definition().id();
    }

    @JsonIgnore
    public String definitionKey() {
        return definition().key();
    }

    @JsonIgnore
    public boolean isCallActivity() {
        return parentId != null;
    }

    @JsonIgnore
    public boolean containsActivityDefinition(String activityDefinitionId) {
        return definition.getActivityById(activityDefinitionId).isPresent();
    }

    public boolean isRootProcess() {
        return parentId == null;
    }

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

}
