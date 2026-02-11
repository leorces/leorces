package com.leorces.model.runtime.activity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.leorces.model.definition.ProcessDefinition;
import com.leorces.model.definition.activity.ActivityDefinition;
import com.leorces.model.definition.activity.ActivityType;
import com.leorces.model.runtime.process.Process;
import com.leorces.model.runtime.variable.Variable;
import com.leorces.model.utils.ActivityUtils;
import lombok.*;
import lombok.extern.jackson.Jacksonized;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * Represents the execution of an activity within a getProcess.
 */
@EqualsAndHashCode
@ToString
@AllArgsConstructor
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
@Builder(toBuilder = true)
@Jacksonized
public class ActivityExecution {

    String id;
    String definitionId;
    Process process;
    List<Variable> variables;
    ActivityState state;
    int retries;
    LocalDateTime timeout;
    ActivityFailure failure;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
    LocalDateTime startedAt;
    LocalDateTime completedAt;
    @JsonIgnore
    private Map<String, Object> scopedVariables;

    /**
     * Gets the variables associated with this activity execution.
     *
     * @return non-null list of variables
     */
    public List<Variable> getVariables() {
        return Objects.requireNonNullElseGet(variables, List::of);
    }

    public Map<String, Object> getScopedVariables(Supplier<Map<String, Object>> scopedVariablesLoader) {
        if (scopedVariables != null) {
            return scopedVariables;
        }
        this.scopedVariables = scopedVariablesLoader.get();
        return scopedVariables;
    }

    /**
     * Gets the activity definition from the getProcess definition.
     *
     * @return the activity definition
     */
    @JsonIgnore
    public ActivityDefinition getDefinition() {
        return Optional.ofNullable(getProcessDefinition())
                .flatMap(processDefinition -> processDefinition.getActivityById(definitionId))
                .orElseThrow();
    }

    /**
     * Gets the getProcess definition of the getProcess this activity belongs to.
     *
     * @return the getProcess definition
     */
    @JsonIgnore
    public ProcessDefinition getProcessDefinition() {
        return process.definition();
    }

    /**
     * Gets the ID of the getProcess definition.
     *
     * @return the getProcess definition ID
     */
    @JsonIgnore
    public String getProcessDefinitionId() {
        return getProcessDefinition().id();
    }

    /**
     * Gets the key of the getProcess definition.
     *
     * @return the getProcess definition key
     */
    @JsonIgnore
    public String getProcessDefinitionKey() {
        return getProcessDefinition().key();
    }

    /**
     * Gets the type of the activity.
     *
     * @return the activity type
     */
    @JsonIgnore
    public ActivityType getType() {
        return getDefinition().type();
    }

    /**
     * Gets the ID of the getProcess instance.
     *
     * @return the getProcess ID
     */
    @JsonIgnore
    public String getProcessId() {
        return process.id();
    }

    /**
     * Gets the next activities based on outgoing sequence flows.
     *
     * @return list of next activity definitions
     */
    @JsonIgnore
    public List<ActivityDefinition> getNextActivities() {
        var outgoingFlows = getDefinition().outgoing();
        return getProcessDefinition().activities().stream()
                .filter(activity -> outgoingFlows.contains(activity.id()))
                .toList();
    }

    /**
     * Gets the previous activities based on incoming sequence flows.
     *
     * @return list of previous activity definitions
     */
    @JsonIgnore
    public List<ActivityDefinition> getPreviousActivities() {
        var incomingFlows = getDefinition().incoming();
        return getProcessDefinition().activities().stream()
                .filter(activity -> incomingFlows.contains(activity.id()))
                .toList();
    }

    /**
     * Gets the activities that are children of this activity (for subprocesses).
     *
     * @return list of child activity definitions
     */
    @JsonIgnore
    public List<ActivityDefinition> getChildActivities() {
        return getProcessDefinition().activities().stream()
                .filter(activity -> activity.parentId() != null && activity.parentId().equals(definitionId))
                .toList();
    }

    /**
     * Gets the scope of this activity execution.
     *
     * @return list of definition IDs in scope
     */
    @JsonIgnore
    public List<String> getScope() {
        return ActivityUtils.buildScope(this);
    }

    /**
     * Gets the input mappings of the activity.
     *
     * @return map of input mappings
     */
    @JsonIgnore
    public Map<String, Object> getInputs() {
        return getDefinition().inputs();
    }

    /**
     * Gets the output mappings of the activity.
     *
     * @return map of output mappings
     */
    @JsonIgnore
    public Map<String, Object> getOutputs() {
        return getDefinition().outputs();
    }

    /**
     * Gets the ID of the parent activity definition.
     *
     * @return parent definition ID or null
     */
    @JsonIgnore
    public String getParentDefinitionId() {
        return getDefinition().parentId();
    }

    /**
     * Checks if this activity has a parent.
     *
     * @return true if has parent
     */
    @JsonIgnore
    public boolean hasParent() {
        return getParentDefinitionId() != null;
    }

    /**
     * Checks if this activity is configured as asynchronous.
     *
     * @return true if asynchronous
     */
    @JsonIgnore
    public boolean isAsync() {
        return ActivityUtils.isAsync(this);
    }

    /**
     * Gets the outgoing sequence flow IDs.
     *
     * @return list of outgoing flow IDs
     */
    @JsonIgnore
    public List<String> getOutgoing() {
        return getDefinition().outgoing();
    }

    /**
     * Checks if the activity state is SCHEDULED.
     *
     * @return true if SCHEDULED
     */
    public boolean isScheduled() {
        return ActivityState.SCHEDULED.equals(state);
    }

    /**
     * Checks if the activity state is ACTIVE.
     *
     * @return true if ACTIVE
     */
    public boolean isActive() {
        return ActivityState.ACTIVE.equals(state);
    }

    /**
     * Checks if the activity state is COMPLETED.
     *
     * @return true if COMPLETED
     */
    public boolean isCompleted() {
        return ActivityState.COMPLETED.equals(state);
    }

    /**
     * Checks if the activity state is TERMINATED.
     *
     * @return true if TERMINATED
     */
    public boolean isTerminated() {
        return ActivityState.TERMINATED.equals(state);
    }

    /**
     * Checks if the activity state is FAILED.
     *
     * @return true if FAILED
     */
    public boolean isFailed() {
        return ActivityState.FAILED.equals(state);
    }

    /**
     * Checks if the activity is in any terminal state.
     *
     * @return true if in terminal state
     */
    public boolean isInTerminalState() {
        return state == ActivityState.TERMINATED
                || state == ActivityState.COMPLETED;
    }

    // Aliases for backward compatibility during migration
    public String id() {
        return id;
    }

    public String definitionId() {
        return definitionId;
    }

    public Process process() {
        return process;
    }

    public List<Variable> variables() {
        return getVariables();
    }

    public Map<String, Object> scopedVariables() {
        return scopedVariables;
    }

    public ActivityState state() {
        return state;
    }

    public int retries() {
        return retries;
    }

    public LocalDateTime timeout() {
        return timeout;
    }

    public ActivityFailure failure() {
        return failure;
    }

    public LocalDateTime createdAt() {
        return createdAt;
    }

    public LocalDateTime updatedAt() {
        return updatedAt;
    }

    public LocalDateTime startedAt() {
        return startedAt;
    }

    public LocalDateTime completedAt() {
        return completedAt;
    }

    public ActivityDefinition definition() {
        return getDefinition();
    }

    public ProcessDefinition processDefinition() {
        return getProcessDefinition();
    }

    public String processDefinitionId() {
        return getProcessDefinitionId();
    }

    public String processDefinitionKey() {
        return getProcessDefinitionKey();
    }

    public ActivityType type() {
        return getType();
    }

    public String processId() {
        return getProcessId();
    }

    public List<ActivityDefinition> nextActivities() {
        return getNextActivities();
    }

    public List<ActivityDefinition> previousActivities() {
        return getPreviousActivities();
    }

    public List<ActivityDefinition> childActivities() {
        return getChildActivities();
    }

    public List<String> scope() {
        return getScope();
    }

    public Map<String, Object> inputs() {
        return getInputs();
    }

    public Map<String, Object> outputs() {
        return getOutputs();
    }

    public String parentDefinitionId() {
        return getParentDefinitionId();
    }

    public List<String> outgoing() {
        return getOutgoing();
    }

}
