package com.leorces.rest.client.model.worker;

import com.leorces.rest.client.handler.ExternalTaskHandler;

public record WorkerContext(
        ExternalTaskHandler handler,
        WorkerMetadata metadata,
        WorkerState state
) {

    public static WorkerContext create(ExternalTaskHandler handler, WorkerMetadata metadata) {
        var state = new WorkerState(metadata.maxConcurrentTasks());
        return new WorkerContext(handler, metadata, state);
    }

}