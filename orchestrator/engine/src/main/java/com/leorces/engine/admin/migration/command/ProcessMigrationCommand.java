package com.leorces.engine.admin.migration.command;

import com.leorces.engine.core.ExecutionCommand;

import java.util.Map;

public record ProcessMigrationCommand(
        Map<String, Object> input
) implements ExecutionCommand {
}
