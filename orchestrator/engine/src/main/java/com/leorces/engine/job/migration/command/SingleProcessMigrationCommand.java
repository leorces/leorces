package com.leorces.engine.job.migration.command;

import com.leorces.engine.core.ExecutionCommand;
import com.leorces.model.job.migration.ProcessMigrationPlan;
import com.leorces.model.runtime.process.ProcessExecution;

public record SingleProcessMigrationCommand(
        ProcessExecution process,
        ProcessMigrationPlan migration
) implements ExecutionCommand {

    public static SingleProcessMigrationCommand of(ProcessExecution process,
                                                   ProcessMigrationPlan migration) {
        return new SingleProcessMigrationCommand(process, migration);
    }

}
