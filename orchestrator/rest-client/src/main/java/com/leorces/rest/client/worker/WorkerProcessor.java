package com.leorces.rest.client.worker;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.leorces.common.mapper.VariablesMapper;
import com.leorces.rest.client.client.TaskRestClient;
import com.leorces.rest.client.metrics.WorkerMetrics;
import com.leorces.rest.client.model.Task;
import com.leorces.rest.client.model.worker.WorkerContext;
import com.leorces.rest.client.service.TaskService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutorService;

@Slf4j
@Component
public class WorkerProcessor {

    private final TaskRestClient client;
    private final TaskService service;
    private final VariablesMapper variablesMapper;
    private final ObjectMapper objectMapper;
    private final ExecutorService executor;
    private final WorkerMetrics workerMetrics;

    public WorkerProcessor(TaskRestClient client,
                           TaskService service,
                           VariablesMapper variablesMapper,
                           ObjectMapper objectMapper,
                           WorkerMetrics workerMetrics,
                           @Qualifier("taskExecutor") ExecutorService executor) {
        this.client = client;
        this.service = service;
        this.variablesMapper = variablesMapper;
        this.objectMapper = objectMapper;
        this.workerMetrics = workerMetrics;
        this.executor = executor;
    }

    public void process(WorkerContext context) {
        var baseInterval = context.metadata().timeUnit().toMillis(context.metadata().interval());
        var state = context.state();

        int freeSlots = state.maxCapacity - state.activeTasks.get();
        double fillPercent = ((double) state.activeTasks.get() / state.maxCapacity) * 100;

        log.debug("Worker '{}' state before polling: activeTasks={}, maxCapacity={}, fillPercent={}, consecutiveFailures={}",
                context.metadata().topic(), state.activeTasks.get(), state.maxCapacity, fillPercent, state.consecutiveFailures.get());

        workerMetrics.recordQueueMetrics(context, fillPercent);

        if (freeSlots <= 0) {
            log.debug("No free slots for worker '{}', skipping poll", context.metadata().topic());
            return;
        }

        if (!state.shouldPoll(baseInterval)) {
            log.debug("Worker '{}' backoff active, skipping poll. Next poll in {} ms",
                    context.metadata().topic(),
                    state.currentBackoffInterval.get() > 0 ? state.currentBackoffInterval.get() : baseInterval);
            return;
        }

        if (fillPercent < context.metadata().fillPercentThreshold() || state.activeTasks.get() == 0) {
            pollTasks(context, freeSlots);
        }
    }

    private void pollTasks(WorkerContext context, int count) {
        var baseInterval = context.metadata().timeUnit().toMillis(context.metadata().interval());
        var state = context.state();
        var metadata = context.metadata();

        try {
            var response = client.poll(metadata.topic(), metadata.processDefinitionKey(), count);
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                var tasks = response.getBody();
                log.debug("Polled {} task(s) for topic '{}'", tasks.size(), metadata.topic());

                workerMetrics.recordTasksPolledMetrics(context, tasks.size());
                state.recordSuccessfulPoll();
                state.activeTasks.addAndGet(tasks.size());
                log.debug("Worker '{}' activeTasks updated: {}/{}",
                        metadata.topic(), state.activeTasks.get(), state.maxCapacity);

                tasks.forEach(task -> executor.execute(() -> executeTask(context, task)));
            } else {
                log.warn("Polling returned {} for topic '{}'", response.getStatusCode(), metadata.topic());
                workerMetrics.recordFailedPollMetrics(context);
                state.recordFailedPoll(baseInterval, metadata.backoffMultiplier(),
                        metadata.timeUnit().toMillis(metadata.maxBackoffInterval()));
            }
        } catch (Exception e) {
            log.error("Polling failed for topic '{}'", metadata.topic(), e);
            workerMetrics.recordFailedPollMetrics(context);
            state.recordFailedPoll(baseInterval, metadata.backoffMultiplier(),
                    metadata.timeUnit().toMillis(metadata.maxBackoffInterval()));
        }
    }

    private void executeTask(WorkerContext context, Task task) {
        var handler = context.handler();
        var state = context.state();
        var topic = context.metadata().topic();

        try {
            var taskToHandle = task.toBuilder()
                    .objectMapper(objectMapper)
                    .variablesMapper(variablesMapper)
                    .build();
            handler.handle(taskToHandle, service);
            workerMetrics.recordTaskCompletedMetrics(context);
            log.debug("Task '{}' completed successfully for topic '{}'", task.id(), topic);
        } catch (Exception e) {
            log.error("Task '{}' failed for topic '{}'", task.id(), topic, e);
            workerMetrics.recordTaskFailedMetrics(context);
            boolean failedSuccessfully = service.fail(task.id());
            if (!failedSuccessfully) {
                log.error("Failed to mark task '{}' as failed for topic '{}'. Task may be in inconsistent state.",
                        task.id(), topic);
            }
        } finally {
            int remaining = state.activeTasks.decrementAndGet();
            log.debug("Task '{}' finished. Worker '{}' remaining activeTasks={}/{}", task.id(), topic, remaining, state.maxCapacity);
        }
    }
}
