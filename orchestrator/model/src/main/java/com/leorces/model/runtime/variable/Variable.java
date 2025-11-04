package com.leorces.model.runtime.variable;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder(toBuilder = true)
public record Variable(
        String id,
        String processId,
        String executionId,
        String executionDefinitionId,
        String varKey,
        String varValue,
        String type,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {

}
