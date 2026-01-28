package com.leorces.model.job;

import lombok.Builder;

import java.time.LocalDateTime;
import java.util.Map;

@Builder(toBuilder = true)
public record Job(
        String id,
        String type,
        JobState state,
        Map<String, Object> input,
        Map<String, Object> output,
        String failureReason,
        String failureTrace,
        int retries,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        LocalDateTime startedAt,
        LocalDateTime completedAt
) {

    public boolean isCreated() {
        return JobState.CREATED.equals(state());
    }

    public boolean isRunning() {
        return JobState.RUNNING.equals(state());
    }

    public boolean isCompleted() {
        return JobState.COMPLETED.equals(state());
    }

    public boolean isFailed() {
        return JobState.FAILED.equals(state());
    }

}
