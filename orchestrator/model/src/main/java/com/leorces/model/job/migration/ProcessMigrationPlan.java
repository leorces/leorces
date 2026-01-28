package com.leorces.model.job.migration;

import lombok.Builder;

import java.util.List;

@Builder(toBuilder = true)
public record ProcessMigrationPlan(
        String definitionKey,
        Integer fromVersion,
        Integer toVersion,
        List<ActivityMigrationInstruction> instructions
) {
}
