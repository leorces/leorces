package com.leorces.rest.client.configuration.properties.resilence;

import org.springframework.boot.context.properties.bind.DefaultValue;

import java.time.Duration;

public record RetryProperties(
        @DefaultValue("3") int maxAttempts,
        @DefaultValue("1s") Duration waitDuration
) {

}
