package com.leorces.rest.client.model.worker;

import java.util.concurrent.TimeUnit;

public record WorkerMetadata(
        String topic,
        String processDefinitionKey,
        long interval,
        long initialDelay,
        int maxConcurrentTasks,
        TimeUnit timeUnit,
        double backoffMultiplier,
        long maxBackoffInterval,
        double fillPercentThreshold
) {

    public WorkerMetadata(String topic,
                          String processDefinitionKey,
                          long interval,
                          long initialDelay,
                          int maxConcurrentTasks,
                          TimeUnit timeUnit
    ) {
        this(
                topic,
                processDefinitionKey,
                interval,
                initialDelay,
                maxConcurrentTasks,
                timeUnit,
                2.0,
                interval * 10,
                70.0
        );
    }

}