package com.leorces.rest.client.model.worker;

import com.leorces.rest.client.handler.TaskHandler;

public record WorkerContext(
        TaskHandler handler,
        WorkerMetadata metadata,
        WorkerState state
) {

    public static WorkerContext create(TaskHandler handler, WorkerMetadata metadata) {
        var state = new WorkerState(metadata.maxConcurrentTasks());
        return new WorkerContext(handler, metadata, state);
    }

}