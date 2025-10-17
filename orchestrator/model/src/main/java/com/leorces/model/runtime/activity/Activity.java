package com.leorces.model.runtime.activity;


import com.leorces.model.runtime.variable.Variable;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;


@Builder(toBuilder = true)
public record Activity(
        String id,
        String definitionId,
        List<Variable> variables,
        ActivityState state,
        int retries,
        ActivityFailure failure,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        LocalDateTime startedAt,
        LocalDateTime completedAt
) {

}
