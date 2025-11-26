package com.leorces.rest.client.configuration.properties.process;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.util.Map;

@ConfigurationProperties(prefix = "leorces.process")
public record ProcessConfigurationProperties(
        @DefaultValue Map<String, WorkerProperties> configuration
) {

}
