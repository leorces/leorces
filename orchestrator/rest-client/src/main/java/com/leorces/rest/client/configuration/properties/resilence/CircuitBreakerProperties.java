package com.leorces.rest.client.configuration.properties.resilence;

import org.springframework.boot.context.properties.bind.DefaultValue;

import java.time.Duration;

public record CircuitBreakerProperties(
        @DefaultValue("50.0") float failureRateThreshold,
        @DefaultValue("30s") Duration waitDurationInOpenState,
        @DefaultValue("10") int slidingWindowSize,
        @DefaultValue("5") int minimumNumberOfCalls,
        @DefaultValue("3") int permittedNumberOfCallsInHalfOpenState
) {

}
