package com.leorces.rest.client.worker;

import com.leorces.common.service.MetricService;
import com.leorces.rest.client.constants.MetricConstants;
import com.leorces.rest.client.model.worker.WorkerContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.function.Consumer;

import static com.leorces.rest.client.constants.MetricConstants.PROCESS_DEFINITION_KEY;
import static com.leorces.rest.client.constants.MetricConstants.TOPIC;

@Component
@RequiredArgsConstructor
public class WorkerMetrics {

    private final MetricService metricService;

    public void recordQueueMetrics(WorkerContext context, double fillPercent) {
        withLabels(context, labels -> {
            var state = context.state();
            metricService.setGauge(MetricConstants.WORKER_QUEUE_ACTIVE_TASKS, state.activeTasks.get(), labels);
            metricService.setGauge(MetricConstants.WORKER_QUEUE_MAX_CAPACITY, state.maxCapacity, labels);
            metricService.setGauge(MetricConstants.WORKER_QUEUE_FILL_PERCENT, fillPercent, labels);
            metricService.setGauge(MetricConstants.WORKER_QUEUE_CONSECUTIVE_FAILURES, state.consecutiveFailures.get(), labels);
            metricService.setGauge(MetricConstants.WORKER_QUEUE_BACKOFF_INTERVAL_MS, state.currentBackoffInterval.get(), labels);
        });
    }

    public void recordTasksPolledMetrics(WorkerContext context, int tasksPolled) {
        withLabels(context, labels -> {
            metricService.incrementCounter(MetricConstants.WORKER_POLL_SUCCESSFUL, labels);
            if (tasksPolled > 0) {
                metricService.incrementCounter(MetricConstants.WORKER_TASKS_POLLED, tasksPolled, labels);
            }
        });
    }

    public void recordFailedPollMetrics(WorkerContext context) {
        withLabels(context, labels ->
                metricService.incrementCounter(MetricConstants.WORKER_POLL_FAILED, labels)
        );
    }

    public void recordTaskCompletedMetrics(WorkerContext context) {
        withLabels(context, labels ->
                metricService.incrementCounter(MetricConstants.WORKER_TASKS_COMPLETED, labels)
        );
    }

    public void recordTaskFailedMetrics(WorkerContext context) {
        withLabels(context, labels ->
                metricService.incrementCounter(MetricConstants.WORKER_TASKS_FAILED, labels)
        );
    }

    private void withLabels(WorkerContext context, Consumer<Map<String, String>> metricsConsumer) {
        var metadata = context.metadata();
        var labels = Map.of(
                TOPIC, metadata.topic(),
                PROCESS_DEFINITION_KEY, metadata.processDefinitionKey()
        );
        metricsConsumer.accept(labels);
    }

}
