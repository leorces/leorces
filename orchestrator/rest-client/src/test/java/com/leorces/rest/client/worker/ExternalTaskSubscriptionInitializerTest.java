package com.leorces.rest.client.worker;

import com.leorces.rest.client.ExternalTaskService;
import com.leorces.rest.client.exception.ClientConfigurationValidationException;
import com.leorces.rest.client.handler.ExternalTaskHandler;
import com.leorces.rest.client.model.ExternalTask;
import com.leorces.rest.client.model.worker.WorkerContext;
import com.leorces.rest.client.model.worker.WorkerMetadata;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationContext;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TaskWorkerInitializer Tests")
class ExternalTaskSubscriptionInitializerTest {

    @Mock
    private WorkerScheduler workerScheduler;

    @Mock
    private WorkerConfigResolver workerConfigResolver;

    @Mock
    private ApplicationContext applicationContext;

    @Mock
    private ApplicationReadyEvent applicationReadyEvent;

    private TaskWorkerInitializer taskWorkerInitializer;

    @BeforeEach
    void setUp() {
        taskWorkerInitializer = new TaskWorkerInitializer(workerScheduler, workerConfigResolver, applicationContext);
    }

    @Test
    @DisplayName("Should start worker when valid ExternalTaskHandler with ExternalTaskSubscription annotation is found")
    void shouldStartWorkerWhenValidTaskHandlerWithTaskWorkerAnnotationIsFound() {
        //Given
        var testHandler = new TestExternalTaskHandler();
        var workerMetadata = createValidWorkerMetadata();

        when(applicationContext.getBeansOfType(ExternalTaskHandler.class))
                .thenReturn(Map.of("testHandler", testHandler));
        when(workerConfigResolver.resolveWorkerConfig(anyString(), any(ExternalTaskSubscription.class))).thenReturn(workerMetadata);

        //When
        taskWorkerInitializer.onApplicationEvent(applicationReadyEvent);

        //Then
        verify(workerConfigResolver).resolveWorkerConfig(anyString(), any(ExternalTaskSubscription.class));
        verify(workerScheduler).startWorker(any(WorkerContext.class));
    }

    @Test
    @DisplayName("Should skip ExternalTaskHandler without ExternalTaskSubscription annotation")
    void shouldSkipTaskHandlerWithoutTaskWorkerAnnotation() {
        //Given
        var noAnnotationHandler = new NoAnnotationExternalTaskHandler();

        when(applicationContext.getBeansOfType(ExternalTaskHandler.class))
                .thenReturn(Map.of("noAnnotationHandler", noAnnotationHandler));

        //When
        taskWorkerInitializer.onApplicationEvent(applicationReadyEvent);

        //Then
        verifyNoInteractions(workerConfigResolver);
        verifyNoInteractions(workerScheduler);
    }

    @Test
    @DisplayName("Should handle empty ExternalTaskHandler beans map")
    void shouldHandleEmptyTaskHandlerBeansMap() {
        //Given
        when(applicationContext.getBeansOfType(ExternalTaskHandler.class)).thenReturn(Map.of());

        //When & Then
        assertDoesNotThrow(() -> taskWorkerInitializer.onApplicationEvent(applicationReadyEvent));

        verifyNoInteractions(workerConfigResolver);
        verifyNoInteractions(workerScheduler);
    }

    @Test
    @DisplayName("Should throw exception when topic is blank")
    void shouldThrowExceptionWhenTopicNameIsBlank() {
        //Given
        var testHandler = new TestExternalTaskHandler();
        var invalidWorkerMetadata = createWorkerMetadata("", "validKey", 5, 0, 1);

        when(applicationContext.getBeansOfType(ExternalTaskHandler.class))
                .thenReturn(Map.of("testHandler", testHandler));
        when(workerConfigResolver.resolveWorkerConfig(anyString(), any(ExternalTaskSubscription.class))).thenReturn(invalidWorkerMetadata);

        //When & Then
        var exception = assertThrows(ClientConfigurationValidationException.class,
                () -> taskWorkerInitializer.onApplicationEvent(applicationReadyEvent));

        var expectedMessage = "Topic cannot be blank for ExternalTaskHandler";
        assert exception.getMessage().contains(expectedMessage);
    }

    @Test
    @DisplayName("Should throw exception when processDefinitionKey is blank")
    void shouldThrowExceptionWhenProcessDefinitionKeyIsBlank() {
        //Given
        var testHandler = new TestExternalTaskHandler();
        var invalidWorkerMetadata = createWorkerMetadata("validTopic", "", 5, 0, 1);

        when(applicationContext.getBeansOfType(ExternalTaskHandler.class))
                .thenReturn(Map.of("testHandler", testHandler));
        when(workerConfigResolver.resolveWorkerConfig(anyString(), any(ExternalTaskSubscription.class))).thenReturn(invalidWorkerMetadata);

        //When & Then
        var exception = assertThrows(ClientConfigurationValidationException.class,
                () -> taskWorkerInitializer.onApplicationEvent(applicationReadyEvent));

        var expectedMessage = "ProcessDefinitionKey cannot be blank for ExternalTaskHandler";
        assert exception.getMessage().contains(expectedMessage);
    }

    @Test
    @DisplayName("Should throw exception when interval is negative or zero")
    void shouldThrowExceptionWhenIntervalIsNegativeOrZero() {
        //Given
        var testHandler = new TestExternalTaskHandler();
        var invalidWorkerMetadata = createWorkerMetadata("validTopic", "validKey", -1, 0, 1);

        when(applicationContext.getBeansOfType(ExternalTaskHandler.class))
                .thenReturn(Map.of("testHandler", testHandler));
        when(workerConfigResolver.resolveWorkerConfig(anyString(), any(ExternalTaskSubscription.class))).thenReturn(invalidWorkerMetadata);

        //When & Then
        var exception = assertThrows(ClientConfigurationValidationException.class,
                () -> taskWorkerInitializer.onApplicationEvent(applicationReadyEvent));

        var expectedMessage = "Interval must be positive for ExternalTaskHandler";
        assert exception.getMessage().contains(expectedMessage);
    }

    @Test
    @DisplayName("Should throw exception when maxConcurrentTasks is negative or zero")
    void shouldThrowExceptionWhenMaxConcurrentTasksIsNegativeOrZero() {
        //Given
        var testHandler = new TestExternalTaskHandler();
        var invalidWorkerMetadata = createWorkerMetadata("validTopic", "validKey", 5, 0, -1);

        when(applicationContext.getBeansOfType(ExternalTaskHandler.class))
                .thenReturn(Map.of("testHandler", testHandler));
        when(workerConfigResolver.resolveWorkerConfig(anyString(), any(ExternalTaskSubscription.class))).thenReturn(invalidWorkerMetadata);

        //When & Then
        var exception = assertThrows(ClientConfigurationValidationException.class,
                () -> taskWorkerInitializer.onApplicationEvent(applicationReadyEvent));

        var expectedMessage = "MaxConcurrentTasks must be positive for ExternalTaskHandler";
        assert exception.getMessage().contains(expectedMessage);
    }

    @Test
    @DisplayName("Should throw exception when initialDelay is negative")
    void shouldThrowExceptionWhenInitialDelayIsNegative() {
        //Given
        var testHandler = new TestExternalTaskHandler();
        var invalidWorkerMetadata = createWorkerMetadata("validTopic", "validKey", 5, -1, 1);

        when(applicationContext.getBeansOfType(ExternalTaskHandler.class))
                .thenReturn(Map.of("testHandler", testHandler));
        when(workerConfigResolver.resolveWorkerConfig(anyString(), any(ExternalTaskSubscription.class))).thenReturn(invalidWorkerMetadata);

        //When & Then
        var exception = assertThrows(ClientConfigurationValidationException.class,
                () -> taskWorkerInitializer.onApplicationEvent(applicationReadyEvent));

        var expectedMessage = "InitialDelay must be non-negative for ExternalTaskHandler";
        assert exception.getMessage().contains(expectedMessage);
    }

    @Test
    @DisplayName("Should accept zero initialDelay as valid")
    void shouldAcceptZeroInitialDelayAsValid() {
        //Given
        var testHandler = new TestExternalTaskHandler();
        var validWorkerMetadata = createWorkerMetadata("validTopic", "validKey", 5, 0, 1);

        when(applicationContext.getBeansOfType(ExternalTaskHandler.class))
                .thenReturn(Map.of("testHandler", testHandler));
        when(workerConfigResolver.resolveWorkerConfig(anyString(), any(ExternalTaskSubscription.class))).thenReturn(validWorkerMetadata);

        //When & Then
        assertDoesNotThrow(() -> taskWorkerInitializer.onApplicationEvent(applicationReadyEvent));

        verify(workerScheduler).startWorker(any(WorkerContext.class));
    }

    @Test
    @DisplayName("Should process multiple TaskHandlers correctly")
    void shouldProcessMultipleTaskHandlersCorrectly() {
        //Given
        var testHandler1 = new TestExternalTaskHandler();
        var testHandler2 = new TestExternalTaskHandler();
        var noAnnotationHandler = new NoAnnotationExternalTaskHandler();
        var validWorkerMetadata = createValidWorkerMetadata();

        when(applicationContext.getBeansOfType(ExternalTaskHandler.class))
                .thenReturn(Map.of(
                        "testHandler1", testHandler1,
                        "testHandler2", testHandler2,
                        "noAnnotationHandler", noAnnotationHandler
                ));
        when(workerConfigResolver.resolveWorkerConfig(anyString(), any(ExternalTaskSubscription.class))).thenReturn(validWorkerMetadata);

        //When
        taskWorkerInitializer.onApplicationEvent(applicationReadyEvent);

        //Then
        verify(workerConfigResolver, times(2)).resolveWorkerConfig(anyString(), any(ExternalTaskSubscription.class));
        verify(workerScheduler, times(2)).startWorker(any(WorkerContext.class));
    }

    private WorkerMetadata createValidWorkerMetadata() {
        return createWorkerMetadata("testTopic", "testProcessKey", 5, 0, 1);
    }

    private WorkerMetadata createWorkerMetadata(String topic,
                                                String processDefinitionKey,
                                                long interval,
                                                long initialDelay,
                                                int maxConcurrentTasks) {
        return new WorkerMetadata(topic, processDefinitionKey, interval, initialDelay, maxConcurrentTasks, TimeUnit.SECONDS);
    }

    // Test implementation of ExternalTaskHandler with ExternalTaskSubscription annotation
    @ExternalTaskSubscription(topicName = "testTopic", processDefinitionKey = "testProcess")
    static class TestExternalTaskHandler implements ExternalTaskHandler {
        @Override
        public void execute(ExternalTask externalTask, ExternalTaskService externalTaskService) {
            // Test implementation - do nothing
        }

    }

    // Test implementation of ExternalTaskHandler without ExternalTaskSubscription annotation
    static class NoAnnotationExternalTaskHandler implements ExternalTaskHandler {
        @Override
        public void execute(ExternalTask externalTask, ExternalTaskService externalTaskService) {
            // Test implementation - do nothing
        }

    }

}