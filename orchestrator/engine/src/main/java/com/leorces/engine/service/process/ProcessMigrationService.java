package com.leorces.engine.service.process;

import com.leorces.api.exception.ExecutionException;
import com.leorces.model.definition.ProcessDefinition;
import com.leorces.model.definition.activity.ActivityDefinition;
import com.leorces.model.job.migration.ActivityMigrationInstruction;
import com.leorces.model.job.migration.ProcessMigrationPlan;
import com.leorces.persistence.DefinitionPersistence;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProcessMigrationService {

    private final DefinitionPersistence definitionPersistence;

    public ProcessMigrationPlan generateMigrationPlan(ProcessMigrationPlan migration) {
        var fromDefinition = getDefinition(migration.definitionKey(), migration.fromVersion());
        var toDefinition = getDefinition(migration.definitionKey(), migration.toVersion());
        return migration.toBuilder()
                .instructions(generateInstructions(fromDefinition, toDefinition))
                .build();
    }

    private List<ActivityMigrationInstruction> generateInstructions(ProcessDefinition fromDefinition, ProcessDefinition toDefinition) {
        return getDeletedActivityIds(fromDefinition, toDefinition).stream()
                .map(deletedActivityId -> new ActivityMigrationInstruction(deletedActivityId, null))
                .toList();
    }

    private ProcessDefinition getDefinition(String definitionKey, Integer version) {
        return definitionPersistence.findByKeyAndVersion(definitionKey, version)
                .orElseThrow(() -> ExecutionException.of("Can't generate migration plan. %s process definition with version %s not found".formatted(definitionKey, version)));
    }

    private List<String> getDeletedActivityIds(ProcessDefinition fromDefinition,
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
