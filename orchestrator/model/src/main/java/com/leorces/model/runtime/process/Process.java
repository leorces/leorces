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
    public boolean isActive() {
        return state == ProcessState.ACTIVE;
    }

    @JsonIgnore
    public boolean isInTerminalState() {
        return state.isTerminal();
    }

}
