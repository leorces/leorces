package com.leorces.rest.client.metrics;

import com.leorces.common.service.MetricService;
import com.leorces.rest.client.handler.TaskHandler;
import com.leorces.rest.client.model.worker.WorkerContext;
import com.leorces.rest.client.model.worker.WorkerMetadata;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("WorkerMetrics Tests")
class WorkerMetricsTest {

    private static final String TOPIC = "test-topic";
    private static final String PROCESS_DEFINITION_KEY = "test-process";
    private static final Map<String, String> EXPECTED_LABELS = Map.of(
            "topic", TOPIC,
            "processDefinitionKey", PROCESS_DEFINITION_KEY
    );

    @Mock
    private MetricService metricService;

    @Mock
    private TaskHandler taskHandler;

    private WorkerMetrics workerMetrics;

    @BeforeEach
    void setUp() {
        workerMetrics = new WorkerMetrics(metricService);
    }

    @Test
    @DisplayName("Should record queue metrics with correct values and labels")
    void shouldRecordQueueMetricsWithCorrectValuesAndLabels() {
        // Given
        var context = createWorkerContext(10);
        var state = context.state();
        state.activeTasks.set(5);
        state.consecutiveFailures.set(2);
        state.currentBackoffInterval.set(1000);
        var fillPercent = 50.0;

        // When
        workerMetrics.recordQueueMetrics(context, fillPercent);

        // Then
        verify(metricService).setGauge("worker.queue.active_tasks", 5.0, EXPECTED_LABELS);
        verify(metricService).setGauge("worker.queue.max_capacity", 10.0, EXPECTED_LABELS);
        verify(metricService).setGauge("worker.queue.fill_percent", 50.0, EXPECTED_LABELS);
        verify(metricService).setGauge("worker.queue.consecutive_failures", 2.0, EXPECTED_LABELS);
        verify(metricService).setGauge("worker.queue.backoff_interval_ms", 1000.0, EXPECTED_LABELS);
    }

    @Test
    @DisplayName("Should record successful poll metrics when tasks are polled")
    void shouldRecordSuccessfulPollMetricsWhenTasksArePolled() {
        // Given
        var context = createWorkerContext(5);
        var tasksPolled = 3;

        // When
        workerMetrics.recordTasksPolledMetrics(context, tasksPolled);

        // Then
        verify(metricService).incrementCounter("worker.poll.successful", EXPECTED_LABELS);
        verify(metricService).incrementCounter("worker.tasks.polled", 3, EXPECTED_LABELS);
    }

    @Test
    @DisplayName("Should record successful poll metrics without task count when no tasks polled")
    void shouldRecordSuccessfulPollMetricsWithoutTaskCountWhenNoTasksPolled() {
        // Given
        var context = createWorkerContext(5);
        var tasksPolled = 0;

        // When
        workerMetrics.recordTasksPolledMetrics(context, tasksPolled);

        // Then
        verify(metricService).incrementCounter("worker.poll.successful", EXPECTED_LABELS);
        // Should not call incrementCounter for tasks.polled when tasksPolled is 0
    }

    @Test
    @DisplayName("Should record failed poll metrics with correct labels")
    void shouldRecordFailedPollMetricsWithCorrectLabels() {
        // Given
        var context = createWorkerContext(5);

        // When
        workerMetrics.recordFailedPollMetrics(context);

        // Then
        verify(metricService).incrementCounter("worker.poll.failed", EXPECTED_LABELS);
    }

    @Test
    @DisplayName("Should record task completed metrics with correct labels")
    void shouldRecordTaskCompletedMetricsWithCorrectLabels() {
        // Given
        var context = createWorkerContext(5);

        // When
        workerMetrics.recordTaskCompletedMetrics(context);

        // Then
        verify(metricService).incrementCounter("worker.tasks.completed", EXPECTED_LABELS);
    }

    @Test
    @DisplayName("Should record task failed metrics with correct labels")
    void shouldRecordTaskFailedMetricsWithCorrectLabels() {
        // Given
        var context = createWorkerContext(5);

        // When
        workerMetrics.recordTaskFailedMetrics(context);

        // Then
        verify(metricService).incrementCounter("worker.tasks.failed", EXPECTED_LABELS);
    }

    @Test
    @DisplayName("Should create consistent labels across all metric methods")
    void shouldCreateConsistentLabelsAcrossAllMetricMethods() {
        // Given
        var context = createWorkerContext(5);
        var state = context.state();
        state.activeTasks.set(1);

        // When
        workerMetrics.recordQueueMetrics(context, 20.0);
        workerMetrics.recordTasksPolledMetrics(context, 1);
        workerMetrics.recordFailedPollMetrics(context);
        workerMetrics.recordTaskCompletedMetrics(context);
        workerMetrics.recordTaskFailedMetrics(context);

        // Then - All methods should use the same labels
        verify(metricService).setGauge(eq("worker.queue.active_tasks"), eq(1.0), eq(EXPECTED_LABELS));
        verify(metricService).incrementCounter(eq("worker.poll.successful"), eq(EXPECTED_LABELS));
        verify(metricService).incrementCounter(eq("worker.poll.failed"), eq(EXPECTED_LABELS));
        verify(metricService).incrementCounter(eq("worker.tasks.completed"), eq(EXPECTED_LABELS));
        verify(metricService).incrementCounter(eq("worker.tasks.failed"), eq(EXPECTED_LABELS));
    }

    @Test
    @DisplayName("Should handle edge case with zero values in queue metrics")
    void shouldHandleEdgeCaseWithZeroValuesInQueueMetrics() {
        // Given
        var context = createWorkerContext(1);
        var fillPercent = 0.0;

        // When
        workerMetrics.recordQueueMetrics(context, fillPercent);

        // Then
        verify(metricService).setGauge("worker.queue.active_tasks", 0.0, EXPECTED_LABELS);
        verify(metricService).setGauge("worker.queue.max_capacity", 1.0, EXPECTED_LABELS);
        verify(metricService).setGauge("worker.queue.fill_percent", 0.0, EXPECTED_LABELS);
        verify(metricService).setGauge("worker.queue.consecutive_failures", 0.0, EXPECTED_LABELS);
        verify(metricService).setGauge("worker.queue.backoff_interval_ms", 0.0, EXPECTED_LABELS);
    }

    private WorkerContext createWorkerContext(int maxCapacity) {
        var metadata = new WorkerMetadata(
                TOPIC,
                PROCESS_DEFINITION_KEY,
                5L,
                0L,
                maxCapacity,
                TimeUnit.SECONDS,
                2.0,
                60L,
                80.0
        );
        return WorkerContext.create(taskHandler, metadata);
    }
}