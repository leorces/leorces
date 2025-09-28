package com.leorces.rest.client.configuration.properties.resilence;

import org.springframework.boot.context.properties.bind.DefaultValue;

public record ResilenceProperties(
        @DefaultValue CircuitBreakerProperties circuitBreaker,
        @DefaultValue RetryProperties retry
) {

}
