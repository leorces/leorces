package com.leorces.rest.client.worker;

import com.leorces.rest.client.configuration.properties.process.ProcessConfigurationProperties;
import com.leorces.rest.client.configuration.properties.process.WorkerConfigProperties;
import com.leorces.rest.client.configuration.properties.process.WorkerProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("WorkerConfigResolver Tests")
class WorkerConfigResolverTest {

    @Mock
    private ProcessConfigurationProperties processConfigurationProperties;

    @Mock
    private TaskWorker taskWorkerAnnotation;

    private WorkerConfigResolver workerConfigResolver;

    @BeforeEach
    void setUp() {
        workerConfigResolver = new WorkerConfigResolver(processConfigurationProperties);
    }

    @Test
    @DisplayName("Should use annotation defaults when no process configuration exists")
    void shouldUseAnnotationDefaultsWhenNoProcessConfigurationExists() {
        //Given
        var topic = "testTopic";
        var processDefinitionKey = "testProcess";
        var interval = 10L;
        var initialDelay = 5L;
        var maxConcurrentTasks = 3;
        var timeUnit = TimeUnit.MINUTES;

        when(taskWorkerAnnotation.topic()).thenReturn(topic);
        when(taskWorkerAnnotation.processDefinitionKey()).thenReturn(processDefinitionKey);
        when(taskWorkerAnnotation.interval()).thenReturn(interval);
        when(taskWorkerAnnotation.initialDelay()).thenReturn(initialDelay);
        when(taskWorkerAnnotation.maxConcurrentTasks()).thenReturn(maxConcurrentTasks);
        when(taskWorkerAnnotation.timeUnit()).thenReturn(timeUnit);
        when(processConfigurationProperties.configuration()).thenReturn(Map.of());

        //When
        var result = workerConfigResolver.resolveWorkerConfig(taskWorkerAnnotation);

        //Then
        assertThat(result).isNotNull();
        assertThat(result.topic()).isEqualTo(topic);
        assertThat(result.processDefinitionKey()).isEqualTo(processDefinitionKey);
        assertThat(result.interval()).isEqualTo(interval);
        assertThat(result.initialDelay()).isEqualTo(initialDelay);
        assertThat(result.maxConcurrentTasks()).isEqualTo(maxConcurrentTasks);
        assertThat(result.timeUnit()).isEqualTo(timeUnit);
    }

    @Test
    @DisplayName("Should use annotation defaults when process configuration exists but no worker config")
    void shouldUseAnnotationDefaultsWhenProcessConfigurationExistsButNoWorkerConfig() {
        //Given
        var topic = "testTopic";
        var processDefinitionKey = "testProcess";
        var interval = 10L;
        var initialDelay = 5L;
        var maxConcurrentTasks = 3;
        var timeUnit = TimeUnit.MINUTES;

        var workerProperties = new WorkerProperties(Map.of());
        var configurationMap = Map.of(processDefinitionKey, workerProperties);

        when(taskWorkerAnnotation.topic()).thenReturn(topic);
        when(taskWorkerAnnotation.processDefinitionKey()).thenReturn(processDefinitionKey);
        when(taskWorkerAnnotation.interval()).thenReturn(interval);
        when(taskWorkerAnnotation.initialDelay()).thenReturn(initialDelay);
        when(taskWorkerAnnotation.maxConcurrentTasks()).thenReturn(maxConcurrentTasks);
        when(taskWorkerAnnotation.timeUnit()).thenReturn(timeUnit);
        when(processConfigurationProperties.configuration()).thenReturn(configurationMap);

        //When
        var result = workerConfigResolver.resolveWorkerConfig(taskWorkerAnnotation);

        //Then
        assertThat(result).isNotNull();
        assertThat(result.topic()).isEqualTo(topic);
        assertThat(result.processDefinitionKey()).isEqualTo(processDefinitionKey);
        assertThat(result.interval()).isEqualTo(interval);
        assertThat(result.initialDelay()).isEqualTo(initialDelay);
        assertThat(result.maxConcurrentTasks()).isEqualTo(maxConcurrentTasks);
        assertThat(result.timeUnit()).isEqualTo(timeUnit);
    }

    @Test
    @DisplayName("Should use worker configuration overrides when available")
    void shouldUseWorkerConfigurationOverridesWhenAvailable() {
        //Given
        var topic = "testTopic";
        var processDefinitionKey = "testProcess";

        // Override values (note correct parameter order: interval, timeUnit, initialDelay, maxConcurrentTasks)
        var configInterval = 20L;
        var configInitialDelay = 10L;
        var configMaxConcurrentTasks = 5;
        var configTimeUnit = TimeUnit.SECONDS;

        var workerConfig = new WorkerConfigProperties(
                configInterval, configTimeUnit, configInitialDelay, configMaxConcurrentTasks
        );
        var workerProperties = new WorkerProperties(Map.of(topic, workerConfig));
        var configurationMap = Map.of(processDefinitionKey, workerProperties);

        when(taskWorkerAnnotation.topic()).thenReturn(topic);
        when(taskWorkerAnnotation.processDefinitionKey()).thenReturn(processDefinitionKey);
        when(processConfigurationProperties.configuration()).thenReturn(configurationMap);

        //When
        var result = workerConfigResolver.resolveWorkerConfig(taskWorkerAnnotation);

        //Then
        assertThat(result).isNotNull();
        assertThat(result.topic()).isEqualTo(topic);
        assertThat(result.processDefinitionKey()).isEqualTo(processDefinitionKey);
        assertThat(result.interval()).isEqualTo(configInterval);
        assertThat(result.initialDelay()).isEqualTo(configInitialDelay);
        assertThat(result.maxConcurrentTasks()).isEqualTo(configMaxConcurrentTasks);
        assertThat(result.timeUnit()).isEqualTo(configTimeUnit);
    }

    @Test
    @DisplayName("Should use annotation defaults when worker config for different topic exists")
    void shouldUseAnnotationDefaultsWhenWorkerConfigForDifferentTopicExists() {
        //Given
        var topic = "testTopic";
        var differentTopic = "differentTopic";
        var processDefinitionKey = "testProcess";
        var interval = 10L;
        var initialDelay = 5L;
        var maxConcurrentTasks = 3;
        var timeUnit = TimeUnit.MINUTES;

        var workerConfig = new WorkerConfigProperties(20L, TimeUnit.SECONDS, 10L, 5);
        var workerProperties = new WorkerProperties(Map.of(differentTopic, workerConfig));
        var configurationMap = Map.of(processDefinitionKey, workerProperties);

        when(taskWorkerAnnotation.topic()).thenReturn(topic);
        when(taskWorkerAnnotation.processDefinitionKey()).thenReturn(processDefinitionKey);
        when(taskWorkerAnnotation.interval()).thenReturn(interval);
        when(taskWorkerAnnotation.initialDelay()).thenReturn(initialDelay);
        when(taskWorkerAnnotation.maxConcurrentTasks()).thenReturn(maxConcurrentTasks);
        when(taskWorkerAnnotation.timeUnit()).thenReturn(timeUnit);
        when(processConfigurationProperties.configuration()).thenReturn(configurationMap);

        //When
        var result = workerConfigResolver.resolveWorkerConfig(taskWorkerAnnotation);

        //Then
        assertThat(result).isNotNull();
        assertThat(result.topic()).isEqualTo(topic);
        assertThat(result.processDefinitionKey()).isEqualTo(processDefinitionKey);
        assertThat(result.interval()).isEqualTo(interval);
        assertThat(result.initialDelay()).isEqualTo(initialDelay);
        assertThat(result.maxConcurrentTasks()).isEqualTo(maxConcurrentTasks);
        assertThat(result.timeUnit()).isEqualTo(timeUnit);
    }

    @Test
    @DisplayName("Should handle null worker config gracefully")
    void shouldHandleNullWorkerConfigGracefully() {
        //Given
        var topic = "testTopic";
        var processDefinitionKey = "testProcess";
        var interval = 10L;
        var initialDelay = 5L;
        var maxConcurrentTasks = 3;
        var timeUnit = TimeUnit.MINUTES;

        var workerMap = new java.util.HashMap<String, WorkerConfigProperties>();
        workerMap.put(topic, null);
        var workerProperties = new WorkerProperties(workerMap);
        var configurationMap = Map.of(processDefinitionKey, workerProperties);

        when(taskWorkerAnnotation.topic()).thenReturn(topic);
        when(taskWorkerAnnotation.processDefinitionKey()).thenReturn(processDefinitionKey);
        when(taskWorkerAnnotation.interval()).thenReturn(interval);
        when(taskWorkerAnnotation.initialDelay()).thenReturn(initialDelay);
        when(taskWorkerAnnotation.maxConcurrentTasks()).thenReturn(maxConcurrentTasks);
        when(taskWorkerAnnotation.timeUnit()).thenReturn(timeUnit);
        when(processConfigurationProperties.configuration()).thenReturn(configurationMap);

        //When
        var result = workerConfigResolver.resolveWorkerConfig(taskWorkerAnnotation);

        //Then
        assertThat(result).isNotNull();
        assertThat(result.topic()).isEqualTo(topic);
        assertThat(result.processDefinitionKey()).isEqualTo(processDefinitionKey);
        assertThat(result.interval()).isEqualTo(interval);
        assertThat(result.initialDelay()).isEqualTo(initialDelay);
        assertThat(result.maxConcurrentTasks()).isEqualTo(maxConcurrentTasks);
        assertThat(result.timeUnit()).isEqualTo(timeUnit);
    }

    @Test
    @DisplayName("Should preserve topic and processDefinitionKey from annotation always")
    void shouldPreserveTopicAndProcessDefinitionKeyFromAnnotationAlways() {
        //Given
        var topic = "originalTopic";
        var processDefinitionKey = "originalProcess";

        var workerConfig = new WorkerConfigProperties(20L, TimeUnit.SECONDS, 10L, 5);
        var workerProperties = new WorkerProperties(Map.of(topic, workerConfig));
        var configurationMap = Map.of(processDefinitionKey, workerProperties);

        when(taskWorkerAnnotation.topic()).thenReturn(topic);
        when(taskWorkerAnnotation.processDefinitionKey()).thenReturn(processDefinitionKey);
        when(processConfigurationProperties.configuration()).thenReturn(configurationMap);

        //When
        var result = workerConfigResolver.resolveWorkerConfig(taskWorkerAnnotation);

        //Then
        assertThat(result.topic()).isEqualTo(topic);
        assertThat(result.processDefinitionKey()).isEqualTo(processDefinitionKey);
    }

    @Test
    @DisplayName("Should handle missing process definition key gracefully")
    void shouldHandleMissingProcessDefinitionKeyGracefully() {
        //Given
        var topic = "testTopic";
        var processDefinitionKey = "testProcess";
        var interval = 15L;
        var initialDelay = 2L;
        var maxConcurrentTasks = 4;
        var timeUnit = TimeUnit.HOURS;

        // Configuration exists for different process definition key
        var workerConfig = new WorkerConfigProperties(30L, TimeUnit.SECONDS, 5L, 2);
        var workerProperties = new WorkerProperties(Map.of(topic, workerConfig));
        var configurationMap = Map.of("differentProcessKey", workerProperties);

        when(taskWorkerAnnotation.topic()).thenReturn(topic);
        when(taskWorkerAnnotation.processDefinitionKey()).thenReturn(processDefinitionKey);
        when(taskWorkerAnnotation.interval()).thenReturn(interval);
        when(taskWorkerAnnotation.initialDelay()).thenReturn(initialDelay);
        when(taskWorkerAnnotation.maxConcurrentTasks()).thenReturn(maxConcurrentTasks);
        when(taskWorkerAnnotation.timeUnit()).thenReturn(timeUnit);
        when(processConfigurationProperties.configuration()).thenReturn(configurationMap);

        //When
        var result = workerConfigResolver.resolveWorkerConfig(taskWorkerAnnotation);

        //Then
        assertThat(result).isNotNull();
        assertThat(result.topic()).isEqualTo(topic);
        assertThat(result.processDefinitionKey()).isEqualTo(processDefinitionKey);
        assertThat(result.interval()).isEqualTo(interval);
        assertThat(result.initialDelay()).isEqualTo(initialDelay);
        assertThat(result.maxConcurrentTasks()).isEqualTo(maxConcurrentTasks);
        assertThat(result.timeUnit()).isEqualTo(timeUnit);
    }
}