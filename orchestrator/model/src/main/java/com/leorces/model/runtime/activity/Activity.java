package com.leorces.model.runtime.activity;

import com.leorces.model.runtime.variable.Variable;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

@Builder(toBuilder = true)
public record Activity(
        String id,
        String definitionId,
        String processId,
        String processBusinessKey,
        String topic,
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
