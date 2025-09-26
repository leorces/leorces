package com.leorces.rest.client.worker;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.leorces.common.mapper.VariablesMapper;
import com.leorces.rest.client.client.TaskRestClient;
import com.leorces.rest.client.handler.TaskHandler;
import com.leorces.rest.client.metrics.WorkerMetrics;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("WorkerProcessor Tests")
class WorkerProcessorTest {

    private static final String TOPIC = "test-topic";
    private static final String PROCESS_DEFINITION_KEY = "test-process";

    @Mock
    private TaskRestClient client;

    @Mock
    private TaskService service;

    @Mock
    private VariablesMapper variablesMapper;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private ExecutorService executor;

    @Mock
    private WorkerMetrics workerMetrics;

    @Mock
    private TaskHandler taskHandler;

    private WorkerProcessor processor;
    private WorkerContext context;

    @BeforeEach
    void setUp() {
        processor = new WorkerProcessor(client, service, variablesMapper, objectMapper, workerMetrics, executor);

        var metadata = createWorkerMetadata();
        context = WorkerContext.create(taskHandler, metadata);
    }

    @Test
    @DisplayName("Should record queue metrics when processing worker")
    void shouldRecordQueueMetricsWhenProcessingWorker() {
        // Given & When
        processor.process(context);

        // Then
        verify(workerMetrics).recordQueueMetrics(eq(context), eq(0.0));
    }

    @Test
    @DisplayName("Should record successful poll metrics when tasks are polled")
    void shouldRecordSuccessfulPollMetricsWhenTasksArePolled() {
        // Given
        var task1 = mock(Task.class);
        var task2 = mock(Task.class);
        var tasks = List.of(task1, task2);
        var response = new ResponseEntity<>(tasks, HttpStatus.OK);
        when(client.poll(TOPIC, PROCESS_DEFINITION_KEY, 1)).thenReturn(response);

        // When
        processor.process(context);

        // Then
        verify(workerMetrics).recordTasksPolledMetrics(eq(context), eq(2));
        verify(workerMetrics).recordQueueMetrics(eq(context), eq(0.0));
    }

    @Test
    @DisplayName("Should record failed poll metrics when polling returns error status")
    void shouldRecordFailedPollMetricsWhenPollingReturnsErrorStatus() {
        // Given
        var response = ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).<List<Task>>build();
        when(client.poll(TOPIC, PROCESS_DEFINITION_KEY, 1)).thenReturn(response);

        // When
        processor.process(context);

        // Then
        verify(workerMetrics).recordFailedPollMetrics(eq(context));
        verify(workerMetrics).recordQueueMetrics(eq(context), eq(0.0));
    }

    @Test
    @DisplayName("Should record failed poll metrics when polling throws exception")
    void shouldRecordFailedPollMetricsWhenPollingThrowsException() {
        // Given
        when(client.poll(TOPIC, PROCESS_DEFINITION_KEY, 1)).thenThrow(new RuntimeException("Poll failed"));

        // When
        processor.process(context);

        // Then
        verify(workerMetrics).recordFailedPollMetrics(eq(context));
        verify(workerMetrics).recordQueueMetrics(eq(context), eq(0.0));
    }

    @Test
    @DisplayName("Should record task completed metrics when task executes successfully")
    void shouldRecordTaskCompletedMetricsWhenTaskExecutesSuccessfully() {
        // Given
        var task = createTask("task-1");
        var tasks = List.of(task);
        var response = new ResponseEntity<>(tasks, HttpStatus.OK);
        when(client.poll(TOPIC, PROCESS_DEFINITION_KEY, 1)).thenReturn(response);

        var taskBuilder = mock(Task.TaskBuilder.class);
        when(task.toBuilder()).thenReturn(taskBuilder);
        when(taskBuilder.objectMapper(objectMapper)).thenReturn(taskBuilder);
        when(taskBuilder.variablesMapper(variablesMapper)).thenReturn(taskBuilder);
        when(taskBuilder.build()).thenReturn(task);

        doAnswer(invocation -> {
            Runnable runnable = invocation.getArgument(0);
            runnable.run();
            return null;
        }).when(executor).execute(any(Runnable.class));

        // When
        processor.process(context);

        // Then
        verify(workerMetrics).recordTaskCompletedMetrics(eq(context));
        verify(workerMetrics).recordTasksPolledMetrics(eq(context), eq(1));
        verify(workerMetrics).recordQueueMetrics(eq(context), eq(0.0));
    }

    @Test
    @DisplayName("Should record task failed metrics when task execution throws exception")
    void shouldRecordTaskFailedMetricsWhenTaskExecutionThrowsException() {
        // Given
        var task = createTask("task-1");
        var tasks = List.of(task);
        var response = new ResponseEntity<>(tasks, HttpStatus.OK);
        when(client.poll(TOPIC, PROCESS_DEFINITION_KEY, 1)).thenReturn(response);

        var taskBuilder = mock(Task.TaskBuilder.class);
        when(task.toBuilder()).thenReturn(taskBuilder);
        when(taskBuilder.objectMapper(objectMapper)).thenReturn(taskBuilder);
        when(taskBuilder.variablesMapper(variablesMapper)).thenReturn(taskBuilder);
        when(taskBuilder.build()).thenReturn(task);

        doThrow(new RuntimeException("Task execution failed")).when(taskHandler).handle(any(Task.class), eq(service));
        when(service.fail("task-1")).thenReturn(true);

        doAnswer(invocation -> {
            Runnable runnable = invocation.getArgument(0);
            runnable.run();
            return null;
        }).when(executor).execute(any(Runnable.class));

        // When
        processor.process(context);

        // Then
        verify(workerMetrics).recordTaskFailedMetrics(eq(context));
        verify(workerMetrics).recordTasksPolledMetrics(eq(context), eq(1));
        verify(workerMetrics).recordQueueMetrics(eq(context), eq(0.0));
    }

    private WorkerMetadata createWorkerMetadata() {
        return new WorkerMetadata(
                TOPIC,
                PROCESS_DEFINITION_KEY,
                5L,
                0L,
                1,
                TimeUnit.SECONDS,
                2.0,
                60L,
                80.0
        );
    }

    private Task createTask(String id) {
        var task = mock(Task.class);
        when(task.id()).thenReturn(id);
        return task;
    }

}