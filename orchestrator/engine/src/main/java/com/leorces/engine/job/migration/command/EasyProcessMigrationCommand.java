package com.leorces.engine.job.migration.command;

import com.leorces.engine.job.common.command.JobCommand;
import com.leorces.model.definition.ProcessDefinition;
import com.leorces.model.job.migration.ProcessMigrationPlan;

import java.util.Map;

public record EasyProcessMigrationCommand(
        ProcessDefinition fromDefinition,
        ProcessDefinition toDefinition,
        ProcessMigrationPlan migration,
        Map<String, Object> input
) implements JobCommand {

    public static EasyProcessMigrationCommand of(ProcessDefinition fromDefinition,
                                                 ProcessDefinition toDefinition,
                                                 ProcessMigrationPlan migration,
                                                 Map<String, Object> input) {
        return new EasyProcessMigrationCommand(fromDefinition, toDefinition, migration, input);
    }

}