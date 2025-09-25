package com.leorces.rest.client.configuration.properties.process;

import java.util.concurrent.TimeUnit;

public record WorkerConfigProperties(
        long interval,
        TimeUnit timeUnit,
        long initialDelay,
        int maxConcurrentTasks
) {

}
