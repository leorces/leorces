package com.leorces.rest.client.configuration.properties.rest;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.time.Duration;

@ConfigurationProperties(prefix = "leorces.rest")
public record RestClientProperties(
        @DefaultValue("http://localhost:8080") String host,
        @DefaultValue("PT30S") Duration connectTimeout,
        @DefaultValue("PT60S") Duration readTimeout
) {

}

