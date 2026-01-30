package com.leorces.engine.admin.migration.command;

import com.leorces.engine.admin.common.command.JobCommand;
import com.leorces.model.definition.ProcessDefinition;
import com.leorces.model.job.migration.ProcessMigrationPlan;

import java.util.Map;

public record ProcessMigrationWithInstructionsCommand(
        ProcessDefinition fromDefinition,
        ProcessDefinition toDefinition,
        ProcessMigrationPlan migration,
        Map<String, Object> input
) implements JobCommand {

    public static ProcessMigrationWithInstructionsCommand of(ProcessDefinition fromDefinition,
                                                             ProcessDefinition toDefinition,
                                                             ProcessMigrationPlan migration,
                                                             Map<String, Object> input) {
        return new ProcessMigrationWithInstructionsCommand(fromDefinition, toDefinition, migration, input);
    }

}