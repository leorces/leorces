package com.leorces.engine.job.migration.command;

import com.leorces.engine.core.ExecutionCommand;
import com.leorces.model.definition.ProcessDefinition;
import com.leorces.model.job.migration.ProcessMigrationPlan;

public record ValidateProcessMigrationCommand(
        ProcessDefinition fromDefinition,
        ProcessDefinition toDefinition,
        ProcessMigrationPlan migration
) implements ExecutionCommand {

    public static ValidateProcessMigrationCommand of(ProcessDefinition fromDefinition,
                                                     ProcessDefinition toDefinition,
                                                     ProcessMigrationPlan migration) {
        return new ValidateProcessMigrationCommand(fromDefinition, toDefinition, migration);
    }

}
