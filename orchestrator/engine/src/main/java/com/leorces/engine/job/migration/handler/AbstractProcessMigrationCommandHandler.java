package com.leorces.engine.job.migration.handler;

import com.leorces.model.definition.ProcessDefinition;
import com.leorces.model.definition.activity.ActivityDefinition;
import com.leorces.model.job.migration.ProcessMigrationPlan;

import java.util.List;
import java.util.stream.Collectors;

public interface AbstractProcessMigrationCommandHandler {

    default boolean isEasyMigration(ProcessDefinition fromDefinition,
                                    ProcessDefinition toDefinition,
                                    ProcessMigrationPlan migration) {
        return getDeletedActivityIds(fromDefinition, toDefinition).isEmpty()
                && migration.instructions().isEmpty();
    }

    default List<String> getDeletedActivityIds(ProcessDefinition fromDefinition,
                                               ProcessDefinition toDefinition) {
        if (fromDefinition == null || toDefinition.activities() == null) {
            return List.of();
        }

        var targetActivityIds = toDefinition.activities().stream()
                .map(ActivityDefinition::id)
                .collect(Collectors.toSet());

        return fromDefinition.activities().stream()
                .map(ActivityDefinition::id)
                .filter(id -> !targetActivityIds.contains(id))
                .toList();
    }

}
