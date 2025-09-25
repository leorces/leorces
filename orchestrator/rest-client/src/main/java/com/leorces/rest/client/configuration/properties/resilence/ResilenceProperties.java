package com.leorces.rest.client.configuration.properties.resilence;

public record ResilenceProperties(
        CircuitBreakerProperties circuitBreaker,
        RetryProperties retry
) {

}
