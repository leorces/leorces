package com.leorces.engine.admin.migration.handler;

import com.leorces.api.exception.ExecutionException;
import com.leorces.engine.admin.migration.command.GenerateProcessMigrationPlanCommand;
import com.leorces.engine.core.ResultCommandHandler;
import com.leorces.model.definition.ProcessDefinition;
import com.leorces.model.job.migration.ActivityMigrationInstruction;
import com.leorces.model.job.migration.ProcessMigrationPlan;
import com.leorces.persistence.DefinitionPersistence;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class GenerateProcessMigrationPlanCommandHandler implements
        AbstractProcessMigrationCommandHandler,
        ResultCommandHandler<GenerateProcessMigrationPlanCommand, ProcessMigrationPlan> {

    private final DefinitionPersistence definitionPersistence;

    @Override
    public ProcessMigrationPlan execute(GenerateProcessMigrationPlanCommand command) {
        var migration = command.migration();
        var fromDefinition = getDefinition(migration.definitionKey(), migration.fromVersion());
        var toDefinition = getDefinition(migration.definitionKey(), migration.toVersion());
        return migration.toBuilder()
                .instructions(generateInstructions(fromDefinition, toDefinition))
                .build();
    }

    @Override
    public Class<GenerateProcessMigrationPlanCommand> getCommandType() {
        return GenerateProcessMigrationPlanCommand.class;
    }

    private List<ActivityMigrationInstruction> generateInstructions(ProcessDefinition fromDefinition,
                                                                    ProcessDefinition toDefinition) {
        return getDeletedActivityIds(fromDefinition, toDefinition).stream()
                .map(deletedActivityId -> new ActivityMigrationInstruction(deletedActivityId, null))
                .toList();
    }

    private ProcessDefinition getDefinition(String definitionKey, Integer version) {
        return definitionPersistence.findByKeyAndVersion(definitionKey, version)
                .orElseThrow(() -> ExecutionException.of("Can't generate migration plan. %s process definition with version %s not found".formatted(definitionKey, version)));
    }

}
