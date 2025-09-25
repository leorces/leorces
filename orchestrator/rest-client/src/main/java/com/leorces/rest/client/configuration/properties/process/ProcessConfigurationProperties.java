package com.leorces.rest.client.configuration.properties.process;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;

@ConfigurationProperties(prefix = "leorces.process")
public record ProcessConfigurationProperties(
        Map<String, WorkerProperties> configuration
) {

}
