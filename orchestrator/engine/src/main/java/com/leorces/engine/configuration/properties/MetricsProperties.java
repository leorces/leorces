package com.leorces.engine.configuration.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "leorces.metrics")
public record MetricsProperties(
        @DefaultValue("true") boolean enabled
) {

}