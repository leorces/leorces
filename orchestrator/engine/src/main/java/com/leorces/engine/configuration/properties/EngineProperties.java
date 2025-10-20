package com.leorces.engine.configuration.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;

@ConfigurationProperties(prefix = "leorces")
public record EngineProperties(
        Map<String, ProcessProperties> processes
) {

}
