package com.leorces.engine.job.migration.handler;

import com.leorces.api.exception.ExecutionException;
import com.leorces.engine.core.CommandHandler;
import com.leorces.engine.job.migration.command.ValidateProcessMigrationCommand;
import com.leorces.model.definition.ProcessDefinition;
import com.leorces.model.definition.activity.ActivityDefinition;
import com.leorces.model.job.migration.ActivityMigrationInstruction;
import com.leorces.model.job.migration.ProcessMigrationPlan;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class ValidateProcessMigrationCommandHandler implements
        AbstractProcessMigrationCommandHandler,
        CommandHandler<ValidateProcessMigrationCommand> {

    private static final String CAN_NOT_MIGRATE_ERROR_MESSAGE = "Can't migrate %s process from %s to %s version";
    private static final String ACTIVITIES_NOT_FOUND_ERROR_MESSAGE = "Activities %s not found in %s definition with %s version";
    private static final String MISSING_INSTRUCTIONS_ERROR_MESSAGE = "Can't migrate process from definitionId: %s to definitionId: %s. Missing instructions for activities: %s";

    /**
     * Handles the validation of a process migration command.
     * Checks if all activities in instructions exist in the respective definitions
     * and ensures all deleted activities have migration instructions.
     *
     * @param command the migration validation command
     */
    @Override
    public void handle(ValidateProcessMigrationCommand command) {
        var fromDefinition = command.fromDefinition();
        var toDefinition = command.toDefinition();
        var migration = command.migration();

        if (isEasyMigration(fromDefinition, toDefinition, migration)) {
            return;
        }

        validateActivitiesExist(fromDefinition, toDefinition, migration);
        validateMissingInstructions(fromDefinition, toDefinition, migration);
    }

    @Override
    public Class<ValidateProcessMigrationCommand> getCommandType() {
        return ValidateProcessMigrationCommand.class;
    }

    private void validateActivitiesExist(ProcessDefinition fromDefinition,
                                         ProcessDefinition toDefinition,
                                         ProcessMigrationPlan migration) {
        validateFromActivitiesExist(fromDefinition, migration);
        validateToActivitiesExist(toDefinition, migration);
    }

    private void validateFromActivitiesExist(ProcessDefinition fromDefinition, ProcessMigrationPlan migration) {
        var invalidIds = getInvalidActivityIds(fromDefinition, migration.instructions(), ActivityMigrationInstruction::fromActivityId);
        if (!invalidIds.isEmpty()) {
            throw ExecutionException.of(
                    CAN_NOT_MIGRATE_ERROR_MESSAGE.formatted(migration.definitionKey(), migration.fromVersion(), migration.toVersion()),
                    ACTIVITIES_NOT_FOUND_ERROR_MESSAGE.formatted(invalidIds, migration.definitionKey(), migration.fromVersion())
            );
        }
    }

    private void validateToActivitiesExist(ProcessDefinition toDefinition, ProcessMigrationPlan migration) {
        var invalidIds = getInvalidActivityIds(toDefinition, migration.instructions(), ActivityMigrationInstruction::toActivityId);
        if (!invalidIds.isEmpty()) {
            throw ExecutionException.of(
                    CAN_NOT_MIGRATE_ERROR_MESSAGE.formatted(migration.definitionKey(), migration.fromVersion(), migration.toVersion()),
                    ACTIVITIES_NOT_FOUND_ERROR_MESSAGE.formatted(invalidIds, migration.definitionKey(), migration.toVersion())
            );
        }
    }

    private void validateMissingInstructions(ProcessDefinition fromDefinition,
                                             ProcessDefinition toDefinition,
                                             ProcessMigrationPlan migration) {
        var deletedActivityIds = getDeletedActivityIds(fromDefinition, toDefinition);
        var missingInstructions = findMissingActivityInstructions(deletedActivityIds, migration.instructions());

        if (!missingInstructions.isEmpty()) {
            throw ExecutionException.of(MISSING_INSTRUCTIONS_ERROR_MESSAGE.formatted(
                    fromDefinition.id(), toDefinition.id(), missingInstructions));
        }
    }

    private List<String> getInvalidActivityIds(ProcessDefinition definition,
                                               List<ActivityMigrationInstruction> instructions,
                                               Function<ActivityMigrationInstruction, String> idExtractor) {
        var definitionActivities = definition.activities().stream()
                .map(ActivityDefinition::id)
                .collect(Collectors.toSet());
        var instructedIds = instructions == null ? List.<ActivityMigrationInstruction>of() : instructions;
        return instructedIds.stream()
                .map(idExtractor)
                .filter(Objects::nonNull)
                .filter(id -> !definitionActivities.contains(id))
                .toList();
    }

    private List<String> findMissingActivityInstructions(List<String> deletedActivityIds,
                                                         List<ActivityMigrationInstruction> instructions) {
        if (deletedActivityIds == null || deletedActivityIds.isEmpty()) {
            return List.of();
        }
        var instructedFromIds = instructions == null ? Set.<String>of() : instructions.stream()
                .map(ActivityMigrationInstruction::fromActivityId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        return deletedActivityIds.stream()
                .filter(id -> !instructedFromIds.contains(id))
                .toList();
    }

}
