package com.leorces.engine.job.migration.command;

import com.leorces.engine.job.common.command.JobCommand;

import java.util.Map;

public record ProcessMigrationCommand(
        Map<String, Object> input
) implements JobCommand {
}
