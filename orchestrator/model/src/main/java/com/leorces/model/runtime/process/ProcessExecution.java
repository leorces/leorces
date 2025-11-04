package com.leorces.model.runtime.process;

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
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        LocalDateTime startedAt,
        LocalDateTime completedAt
) {

}
