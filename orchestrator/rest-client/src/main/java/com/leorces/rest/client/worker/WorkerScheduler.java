package com.leorces.rest.client.worker;

import com.leorces.rest.client.model.worker.WorkerContext;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

@Component
public class WorkerScheduler {

    private final ExternalTaskSubscriptionProcessor processor;
    private final ScheduledExecutorService scheduler;
    private final AtomicBoolean isShuttingDown = new AtomicBoolean(false);

    public WorkerScheduler(ExternalTaskSubscriptionProcessor processor,
                           @Qualifier("scheduledExecutorService") ScheduledExecutorService scheduler) {
        this.processor = processor;
        this.scheduler = scheduler;
    }

    public void startWorker(WorkerContext context) {
        var metadata = context.metadata();
        var initialDelay = metadata.timeUnit().toMillis(metadata.initialDelay());
        var interval = metadata.timeUnit().toMillis(metadata.interval());

        scheduler.scheduleAtFixedRate(() -> {
            if (!isShuttingDown.get()) {
                processor.process(context);
            }
        }, initialDelay, interval, TimeUnit.MILLISECONDS);
    }

    @PreDestroy
    public void shutdown() {
        isShuttingDown.set(true);
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(30, TimeUnit.SECONDS)) scheduler.shutdownNow();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            scheduler.shutdownNow();
        }
    }

}
