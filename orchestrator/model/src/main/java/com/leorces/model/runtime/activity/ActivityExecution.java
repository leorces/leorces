package com.leorces.model.runtime.activity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.leorces.model.definition.ProcessDefinition;
import com.leorces.model.definition.activity.ActivityDefinition;
import com.leorces.model.definition.activity.ActivityType;
import com.leorces.model.runtime.process.Process;
import com.leorces.model.runtime.variable.Variable;
import com.leorces.model.utils.ActivityUtils;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Builder(toBuilder = true)
public record ActivityExecution(
        String id,
        String definitionId,
        Process process,
        List<Variable> variables,
        ActivityState state,
        int retries,
        LocalDateTime timeout,
        ActivityFailure failure,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        LocalDateTime startedAt,
        LocalDateTime completedAt
) {

    public List<Variable> variables() {
        return Objects.requireNonNullElseGet(variables, List::of);
    }

    @JsonIgnore
    public ActivityDefinition getDefinition() {
        return definition();
    }

    @JsonIgnore
    public ActivityDefinition definition() {
        return Optional.ofNullable(processDefinition())
                .flatMap(processDefinition -> processDefinition.getActivityById(definitionId))
                .orElseThrow();
    }

    @JsonIgnore
    public ProcessDefinition processDefinition() {
        return process.definition();
    }

    @JsonIgnore
    public String processDefinitionId() {
        return processDefinition().id();
    }

    @JsonIgnore
    public String processDefinitionKey() {
        return processDefinition().key();
    }

    @JsonIgnore
    public ActivityType type() {
        return definition().type();
    }

    @JsonIgnore
    public String processId() {
        return process.id();
    }

    @JsonIgnore
    public List<ActivityDefinition> nextActivities() {
        var outgoing = definition().outgoing();
        return processDefinition().activities().stream()
                .filter(activity -> outgoing.contains(activity.id()))
                .toList();
    }

    @JsonIgnore
    public List<ActivityDefinition> previousActivities() {
        var incoming = definition().incoming();
        return processDefinition().activities().stream()
                .filter(activity -> incoming.contains(activity.id()))
                .toList();
    }

    @JsonIgnore
    public List<ActivityDefinition> childActivities() {
        return processDefinition().activities().stream()
                .filter(activity -> activity.parentId() != null && activity.parentId().equals(definitionId))
                .toList();
    }

    @JsonIgnore
    public List<String> scope() {
        return ActivityUtils.buildScope(this);
    }

    @JsonIgnore
    public Map<String, Object> inputs() {
        return definition().inputs();
    }

    @JsonIgnore
    public Map<String, Object> outputs() {
        return definition().outputs();
    }

    @JsonIgnore
    public String parentDefinitionId() {
        return definition().parentId();
    }

    @JsonIgnore
    public boolean hasParent() {
        return parentDefinitionId() != null;
    }

    @JsonIgnore
    public boolean isAsync() {
        return ActivityUtils.isAsync(this);
    }

    @JsonIgnore
    public List<String> outgoing() {
        return definition().outgoing();
    }

    public boolean isScheduled() {
        return ActivityState.SCHEDULED.equals(state());
    }

    public boolean isActive() {
        return ActivityState.ACTIVE.equals(state());
    }

    public boolean isCompleted() {
        return ActivityState.COMPLETED.equals(state());
    }

    public boolean isTerminated() {
        return ActivityState.TERMINATED.equals(state());
    }

    public boolean isFailed() {
        return ActivityState.FAILED.equals(state());
    }

    public boolean isInTerminalState() {
        return state == ActivityState.TERMINATED || state == ActivityState.COMPLETED;
    }

}
