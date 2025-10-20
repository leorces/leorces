package com.leorces.engine.configuration.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.util.Map;

@ConfigurationProperties(prefix = "leorces.processes")
public record ProcessProperties(
        @DefaultValue("0") int activityRetries,
        @DefaultValue("1h") String activityTimeout,
        Map<String, ActivityProperties> activities
) {

}
