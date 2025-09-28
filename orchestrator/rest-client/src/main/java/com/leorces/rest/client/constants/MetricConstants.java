package com.leorces.rest.client.constants;

public final class MetricConstants {
    // =====================
    // Queue metrics
    // =====================
    public static final String WORKER_QUEUE_ACTIVE_TASKS = "worker.queue.active_tasks";
    public static final String WORKER_QUEUE_MAX_CAPACITY = "worker.queue.max_capacity";
    public static final String WORKER_QUEUE_FILL_PERCENT = "worker.queue.fill_percent";
    public static final String WORKER_QUEUE_CONSECUTIVE_FAILURES = "worker.queue.consecutive_failures";
    public static final String WORKER_QUEUE_BACKOFF_INTERVAL_MS = "worker.queue.backoff_interval_ms";
    // =====================
    //  Poll metrics
    // =====================
    public static final String WORKER_POLL_SUCCESSFUL = "worker.poll.successful";
    public static final String WORKER_POLL_FAILED = "worker.poll.failed";
    public static final String WORKER_TASKS_POLLED = "worker.tasks.polled";
    // =====================
    //  Task execution metrics
    // =====================
    public static final String WORKER_TASKS_COMPLETED = "worker.tasks.completed";
    public static final String WORKER_TASKS_FAILED = "worker.tasks.failed";
    // =====================
    // Labels
    // =====================
    public static final String PROCESS_DEFINITION_KEY = "processDefinitionKey";
    public static final String TOPIC = "topic";

    private MetricConstants() {

    }

}
