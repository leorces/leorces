package com.leorces.rest.client.configuration;

import com.leorces.common.service.MetricService;
import com.leorces.common.service.impl.MetricServiceImpl;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("RestClientAutoConfiguration Tests")
class RestClientAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(RestClientAutoConfiguration.class))
            .withUserConfiguration(TestConfiguration.class);

    @Test
    @DisplayName("Should create MetricServiceImpl bean when metrics are enabled")
    void shouldCreateMetricServiceImplBeanWhenMetricsAreEnabled() {
        // Given & When
        contextRunner
                .withPropertyValues("leorces.metrics.enabled=true")
                .run(context -> {
                    // Then
                    assertThat(context).hasSingleBean(MetricService.class);
                    var metricService = context.getBean(MetricService.class);
                    assertThat(metricService).isNotNull();
                    assertThat(metricService).isInstanceOf(MetricServiceImpl.class);
                });
    }

    @Test
    @DisplayName("Should create MetricServiceImpl bean by default when no property is set")
    void shouldCreateMetricServiceImplBeanByDefaultWhenNoPropertyIsSet() {
        // Given & When
        contextRunner
                .run(context -> {
                    // Then
                    assertThat(context).hasSingleBean(MetricService.class);
                    var metricService = context.getBean(MetricService.class);
                    assertThat(metricService).isNotNull();
                    assertThat(metricService).isInstanceOf(MetricServiceImpl.class);
                });
    }

    @Test
    @DisplayName("Should not create MetricService bean when metrics are disabled")
    void shouldNotCreateMetricServiceBeanWhenMetricsAreDisabled() {
        // Given & When
        contextRunner
                .withPropertyValues("leorces.metrics.enabled=false")
                .run(context -> {
                    // Then
                    assertThat(context).doesNotHaveBean(MetricService.class);
                });
    }

    @Test
    @DisplayName("Should create real RestClient when leorces.rest.host is provided")
    void shouldCreateRealRestClientWhenHostIsProvided() {
        contextRunner
                .withPropertyValues("leorces.rest.host=http://example.com")
                .run(context -> {
                    assertThat(context).hasBean("leorcesRestClient");
                    assertThat(context.getBean("leorcesRestClient")).isInstanceOf(RestClient.class);
                });
    }

    @Test
    @DisplayName("Should create mock RestClient when leorces.rest.host is missing")
    void shouldCreateMockRestClientWhenHostIsMissing() {
        contextRunner
                .run(context -> {
                    assertThat(context).hasBean("leorcesRestClient");
                    var restClient = (RestClient) context.getBean("leorcesRestClient");

                    assertThrows(IllegalStateException.class, () -> restClient.get().uri("/test").retrieve().toBodilessEntity());
                });
    }

    @Configuration
    static class TestConfiguration {

        @Bean
        public MeterRegistry meterRegistry() {
            return new SimpleMeterRegistry();
        }

    }

}