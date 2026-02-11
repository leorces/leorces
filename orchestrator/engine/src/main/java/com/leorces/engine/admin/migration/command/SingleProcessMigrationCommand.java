package com.leorces.engine.admin.migration.command;

import com.leorces.engine.core.ExecutionCommand;
import com.leorces.model.definition.ProcessDefinition;
import com.leorces.model.job.migration.ProcessMigrationPlan;
import com.leorces.model.runtime.process.ProcessExecution;

public record SingleProcessMigrationCommand(
        ProcessExecution process,
        ProcessDefinition toDefinition,
        ProcessMigrationPlan migration
) implements ExecutionCommand {

    public static SingleProcessMigrationCommand of(ProcessExecution process,
                                                   ProcessDefinition toDefinition,
                                                   ProcessMigrationPlan migration) {
        return new SingleProcessMigrationCommand(process, toDefinition, migration);
    }

}
