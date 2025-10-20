package com.leorces.engine.configuration;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.leorces.common.service.MetricService;
import com.leorces.common.service.impl.MetricServiceImpl;
import com.leorces.common.service.impl.MicrometerMetricService;
import com.leorces.engine.configuration.properties.CompactionProperties;
import com.leorces.engine.configuration.properties.EngineProperties;
import com.leorces.engine.configuration.properties.MetricsProperties;
import com.leorces.engine.configuration.properties.ProcessProperties;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.core.task.support.TaskExecutorAdapter;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.concurrent.Executors;

@Slf4j
@EnableAsync
@EnableScheduling
@AutoConfiguration
@RequiredArgsConstructor
@EnableConfigurationProperties({
        EngineProperties.class,
        MetricsProperties.class,
        CompactionProperties.class,
        ProcessProperties.class
})
public class OrchestratorEngineAutoConfiguration {

    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        return new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @Bean
    @Qualifier("engineTaskExecutor")
    public AsyncTaskExecutor engineTaskExecutor() {
        return new TaskExecutorAdapter(Executors.newVirtualThreadPerTaskExecutor());
    }

    @Bean
    @ConditionalOnProperty(name = "leorces.metrics.enabled", havingValue = "true", matchIfMissing = true)
    public MetricService metricService(MeterRegistry meterRegistry, MetricsProperties metricsProperties) {
        var micrometerService = new MicrometerMetricService(meterRegistry);
        return new MetricServiceImpl(micrometerService, metricsProperties.enabled());
    }

}
