package com.leorces.rest.client.configuration;

import com.leorces.rest.client.configuration.properties.resilence.ResilenceConfigurationProperties;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CircuitBreakerConfiguration {

    @Bean
    public CircuitBreakerRegistry circuitBreakerRegistry(
            ResilenceConfigurationProperties resilenceConfigurationProperties
    ) {
        var registry = CircuitBreakerRegistry.ofDefaults();

        // Register circuit breaker for task-poll
        var taskPollProperties = resilenceConfigurationProperties.taskPoll().circuitBreaker();
        var taskPollConfig = CircuitBreakerConfig.custom()
                .failureRateThreshold(taskPollProperties.failureRateThreshold())
                .waitDurationInOpenState(taskPollProperties.waitDurationInOpenState())
                .slidingWindowSize(taskPollProperties.slidingWindowSize())
                .minimumNumberOfCalls(taskPollProperties.minimumNumberOfCalls())
                .permittedNumberOfCallsInHalfOpenState(taskPollProperties.permittedNumberOfCallsInHalfOpenState())
                .build();

        // Register circuit breaker for task-update
        var taskUpdateProperties = resilenceConfigurationProperties.taskUpdate().circuitBreaker();
        var taskUpdateConfig = CircuitBreakerConfig.custom()
                .failureRateThreshold(taskUpdateProperties.failureRateThreshold())
                .waitDurationInOpenState(taskUpdateProperties.waitDurationInOpenState())
                .slidingWindowSize(taskUpdateProperties.slidingWindowSize())
                .minimumNumberOfCalls(taskUpdateProperties.minimumNumberOfCalls())
                .permittedNumberOfCallsInHalfOpenState(taskUpdateProperties.permittedNumberOfCallsInHalfOpenState())
                .build();

        registry.circuitBreaker("task-poll", taskPollConfig);
        registry.circuitBreaker("task-update", taskUpdateConfig);

        return registry;
    }

    @Bean
    public RetryRegistry retryRegistry(
            ResilenceConfigurationProperties resilenceConfigurationProperties
    ) {
        var registry = RetryRegistry.ofDefaults();

        // Register retry only for task-update (task-poll doesn't need retries)
        if (resilenceConfigurationProperties.taskUpdate().retry() != null) {
            var retryProps = resilenceConfigurationProperties.taskUpdate().retry();
            var taskUpdateRetryConfig = RetryConfig.custom()
                    .maxAttempts(retryProps.maxAttempts())
                    .waitDuration(retryProps.waitDuration())
                    .build();

            registry.retry("task-update", taskUpdateRetryConfig);
        }

        return registry;
    }

}
