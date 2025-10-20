package com.leorces.engine.configuration.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "leorces.compaction")
public record CompactionProperties(
        @DefaultValue("false") boolean enabled,
        @DefaultValue("1000") int batchSize,
        @DefaultValue("0 0 0 * * *") String cron
) {

}
