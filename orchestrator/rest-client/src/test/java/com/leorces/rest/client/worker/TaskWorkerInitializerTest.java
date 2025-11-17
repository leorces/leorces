package com.leorces.rest.client.worker;

import com.leorces.rest.client.exception.ClientConfigurationValidationException;
import com.leorces.rest.client.handler.TaskHandler;
import com.leorces.rest.client.model.Task;
import com.leorces.rest.client.model.worker.WorkerContext;
import com.leorces.rest.client.model.worker.WorkerMetadata;
import com.leorces.rest.client.service.TaskService;
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
class TaskWorkerInitializerTest {

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
    @DisplayName("Should start worker when valid TaskHandler with TaskWorker annotation is found")
    void shouldStartWorkerWhenValidTaskHandlerWithTaskWorkerAnnotationIsFound() {
        //Given
        var testHandler = new TestTaskHandler();
        var workerMetadata = createValidWorkerMetadata();

        when(applicationContext.getBeansOfType(TaskHandler.class))
                .thenReturn(Map.of("testHandler", testHandler));
        when(workerConfigResolver.resolveWorkerConfig(any(TaskWorker.class))).thenReturn(workerMetadata);

        //When
        taskWorkerInitializer.onApplicationEvent(applicationReadyEvent);

        //Then
        verify(workerConfigResolver).resolveWorkerConfig(any(TaskWorker.class));
        verify(workerScheduler).startWorker(any(WorkerContext.class));
    }

    @Test
    @DisplayName("Should skip TaskHandler without TaskWorker annotation")
    void shouldSkipTaskHandlerWithoutTaskWorkerAnnotation() {
        //Given
        var noAnnotationHandler = new NoAnnotationTaskHandler();

        when(applicationContext.getBeansOfType(TaskHandler.class))
                .thenReturn(Map.of("noAnnotationHandler", noAnnotationHandler));

        //When
        taskWorkerInitializer.onApplicationEvent(applicationReadyEvent);

        //Then
        verifyNoInteractions(workerConfigResolver);
        verifyNoInteractions(workerScheduler);
    }

    @Test
    @DisplayName("Should handle empty TaskHandler beans map")
    void shouldHandleEmptyTaskHandlerBeansMap() {
        //Given
        when(applicationContext.getBeansOfType(TaskHandler.class)).thenReturn(Map.of());

        //When & Then
        assertDoesNotThrow(() -> taskWorkerInitializer.onApplicationEvent(applicationReadyEvent));

        verifyNoInteractions(workerConfigResolver);
        verifyNoInteractions(workerScheduler);
    }

    @Test
    @DisplayName("Should throw exception when topic is blank")
    void shouldThrowExceptionWhenTopicIsBlank() {
        //Given
        var testHandler = new TestTaskHandler();
        var invalidWorkerMetadata = createWorkerMetadata("", "validKey", 5, 0, 1);

        when(applicationContext.getBeansOfType(TaskHandler.class))
                .thenReturn(Map.of("testHandler", testHandler));
        when(workerConfigResolver.resolveWorkerConfig(any(TaskWorker.class))).thenReturn(invalidWorkerMetadata);

        //When & Then
        var exception = assertThrows(ClientConfigurationValidationException.class,
                () -> taskWorkerInitializer.onApplicationEvent(applicationReadyEvent));

        var expectedMessage = "Topic cannot be blank for TaskHandler";
        assert exception.getMessage().contains(expectedMessage);
    }

    @Test
    @DisplayName("Should throw exception when processDefinitionKey is blank")
    void shouldThrowExceptionWhenProcessDefinitionKeyIsBlank() {
        //Given
        var testHandler = new TestTaskHandler();
        var invalidWorkerMetadata = createWorkerMetadata("validTopic", "", 5, 0, 1);

        when(applicationContext.getBeansOfType(TaskHandler.class))
                .thenReturn(Map.of("testHandler", testHandler));
        when(workerConfigResolver.resolveWorkerConfig(any(TaskWorker.class))).thenReturn(invalidWorkerMetadata);

        //When & Then
        var exception = assertThrows(ClientConfigurationValidationException.class,
                () -> taskWorkerInitializer.onApplicationEvent(applicationReadyEvent));

        var expectedMessage = "ProcessDefinitionKey cannot be blank for TaskHandler";
        assert exception.getMessage().contains(expectedMessage);
    }

    @Test
    @DisplayName("Should throw exception when interval is negative or zero")
    void shouldThrowExceptionWhenIntervalIsNegativeOrZero() {
        //Given
        var testHandler = new TestTaskHandler();
        var invalidWorkerMetadata = createWorkerMetadata("validTopic", "validKey", -1, 0, 1);

        when(applicationContext.getBeansOfType(TaskHandler.class))
                .thenReturn(Map.of("testHandler", testHandler));
        when(workerConfigResolver.resolveWorkerConfig(any(TaskWorker.class))).thenReturn(invalidWorkerMetadata);

        //When & Then
        var exception = assertThrows(ClientConfigurationValidationException.class,
                () -> taskWorkerInitializer.onApplicationEvent(applicationReadyEvent));

        var expectedMessage = "Interval must be positive for TaskHandler";
        assert exception.getMessage().contains(expectedMessage);
    }

    @Test
    @DisplayName("Should throw exception when maxConcurrentTasks is negative or zero")
    void shouldThrowExceptionWhenMaxConcurrentTasksIsNegativeOrZero() {
        //Given
        var testHandler = new TestTaskHandler();
        var invalidWorkerMetadata = createWorkerMetadata("validTopic", "validKey", 5, 0, -1);

        when(applicationContext.getBeansOfType(TaskHandler.class))
                .thenReturn(Map.of("testHandler", testHandler));
        when(workerConfigResolver.resolveWorkerConfig(any(TaskWorker.class))).thenReturn(invalidWorkerMetadata);

        //When & Then
        var exception = assertThrows(ClientConfigurationValidationException.class,
                () -> taskWorkerInitializer.onApplicationEvent(applicationReadyEvent));

        var expectedMessage = "MaxConcurrentTasks must be positive for TaskHandler";
        assert exception.getMessage().contains(expectedMessage);
    }

    @Test
    @DisplayName("Should throw exception when initialDelay is negative")
    void shouldThrowExceptionWhenInitialDelayIsNegative() {
        //Given
        var testHandler = new TestTaskHandler();
        var invalidWorkerMetadata = createWorkerMetadata("validTopic", "validKey", 5, -1, 1);

        when(applicationContext.getBeansOfType(TaskHandler.class))
                .thenReturn(Map.of("testHandler", testHandler));
        when(workerConfigResolver.resolveWorkerConfig(any(TaskWorker.class))).thenReturn(invalidWorkerMetadata);

        //When & Then
        var exception = assertThrows(ClientConfigurationValidationException.class,
                () -> taskWorkerInitializer.onApplicationEvent(applicationReadyEvent));

        var expectedMessage = "InitialDelay must be non-negative for TaskHandler";
        assert exception.getMessage().contains(expectedMessage);
    }

    @Test
    @DisplayName("Should accept zero initialDelay as valid")
    void shouldAcceptZeroInitialDelayAsValid() {
        //Given
        var testHandler = new TestTaskHandler();
        var validWorkerMetadata = createWorkerMetadata("validTopic", "validKey", 5, 0, 1);

        when(applicationContext.getBeansOfType(TaskHandler.class))
                .thenReturn(Map.of("testHandler", testHandler));
        when(workerConfigResolver.resolveWorkerConfig(any(TaskWorker.class))).thenReturn(validWorkerMetadata);

        //When & Then
        assertDoesNotThrow(() -> taskWorkerInitializer.onApplicationEvent(applicationReadyEvent));

        verify(workerScheduler).startWorker(any(WorkerContext.class));
    }

    @Test
    @DisplayName("Should process multiple TaskHandlers correctly")
    void shouldProcessMultipleTaskHandlersCorrectly() {
        //Given
        var testHandler1 = new TestTaskHandler();
        var testHandler2 = new TestTaskHandler();
        var noAnnotationHandler = new NoAnnotationTaskHandler();
        var validWorkerMetadata = createValidWorkerMetadata();

        when(applicationContext.getBeansOfType(TaskHandler.class))
                .thenReturn(Map.of(
                        "testHandler1", testHandler1,
                        "testHandler2", testHandler2,
                        "noAnnotationHandler", noAnnotationHandler
                ));
        when(workerConfigResolver.resolveWorkerConfig(any(TaskWorker.class))).thenReturn(validWorkerMetadata);

        //When
        taskWorkerInitializer.onApplicationEvent(applicationReadyEvent);

        //Then
        verify(workerConfigResolver, times(2)).resolveWorkerConfig(any(TaskWorker.class));
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

    // Test implementation of TaskHandler with TaskWorker annotation
    @TaskWorker(topic = "testTopic", processDefinitionKey = "testProcess")
    static class TestTaskHandler implements TaskHandler {
        @Override
        public void handle(Task task, TaskService taskService) {
            // Test implementation - do nothing
        }

    }

    // Test implementation of TaskHandler without TaskWorker annotation
    static class NoAnnotationTaskHandler implements TaskHandler {
        @Override
        public void handle(Task task, TaskService taskService) {
            // Test implementation - do nothing
        }

    }

}