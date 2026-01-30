package com.leorces.engine.admin.migration.command;

import com.leorces.engine.core.ExecutionResultCommand;
import com.leorces.model.job.migration.ProcessMigrationPlan;

public record GenerateProcessMigrationPlanCommand(
        ProcessMigrationPlan migration
) implements ExecutionResultCommand<ProcessMigrationPlan> {
}
