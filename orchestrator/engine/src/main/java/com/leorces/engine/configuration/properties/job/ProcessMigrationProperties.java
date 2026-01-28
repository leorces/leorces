package com.leorces.engine.configuration.properties.job;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "leorces.jobs.process-migration")
public record ProcessMigrationProperties(
        @DefaultValue("1000") int batchSize,
        @DefaultValue("1") int maxJobs
) {
}
