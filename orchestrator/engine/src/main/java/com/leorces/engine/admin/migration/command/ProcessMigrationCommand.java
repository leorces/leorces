package com.leorces.engine.admin.migration.command;

import com.leorces.engine.admin.common.command.JobCommand;

import java.util.Map;

public record ProcessMigrationCommand(
        Map<String, Object> input
) implements JobCommand {
}
