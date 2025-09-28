package com.leorces.rest.client.worker;

import com.leorces.common.service.MetricService;
import com.leorces.rest.client.model.worker.WorkerContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.function.Consumer;

@Component
@RequiredArgsConstructor
public class WorkerMetrics {

    private final MetricService metricService;

    public void recordQueueMetrics(WorkerContext context, double fillPercent) {
        withLabels(context, labels -> {
            var state = context.state();
            metricService.setGauge("worker.queue.active_tasks", state.activeTasks.get(), labels);
            metricService.setGauge("worker.queue.max_capacity", state.maxCapacity, labels);
            metricService.setGauge("worker.queue.fill_percent", fillPercent, labels);
            metricService.setGauge("worker.queue.consecutive_failures", state.consecutiveFailures.get(), labels);
            metricService.setGauge("worker.queue.backoff_interval_ms", state.currentBackoffInterval.get(), labels);
        });
    }

    public void recordTasksPolledMetrics(WorkerContext context, int tasksPolled) {
        withLabels(context, labels -> {
            metricService.incrementCounter("worker.poll.successful", labels);
            if (tasksPolled > 0) {
                metricService.incrementCounter("worker.tasks.polled", tasksPolled, labels);
            }
        });
    }

    public void recordFailedPollMetrics(WorkerContext context) {
        withLabels(context, labels ->
                metricService.incrementCounter("worker.poll.failed", labels)
        );
    }

    public void recordTaskCompletedMetrics(WorkerContext context) {
        withLabels(context, labels ->
                metricService.incrementCounter("worker.tasks.completed", labels)
        );
    }

    public void recordTaskFailedMetrics(WorkerContext context) {
        withLabels(context, labels ->
                metricService.incrementCounter("worker.tasks.failed", labels)
        );
    }

    private void withLabels(WorkerContext context, Consumer<Map<String, String>> metricsConsumer) {
        var metadata = context.metadata();
        var labels = Map.of(
                "topic", metadata.topic(),
                "processDefinitionKey", metadata.processDefinitionKey()
        );
        metricsConsumer.accept(labels);
    }

}
