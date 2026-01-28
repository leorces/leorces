package com.leorces.model.job.migration;

public record ActivityMigrationInstruction(
        String fromActivityId,
        String toActivityId
) {
}
