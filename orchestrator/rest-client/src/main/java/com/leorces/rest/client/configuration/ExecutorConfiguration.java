package com.leorces.rest.client.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

@Configuration
public class ExecutorConfiguration {

    @Bean("taskExecutor")
    public ExecutorService taskExecutor() {
        return Executors.newCachedThreadPool(r -> {
            var thread = new Thread(r);
            thread.setName("task-worker-%d".formatted(thread.threadId()));
            thread.setDaemon(false);
            return thread;
        });
    }

    @Bean("scheduledExecutorService")
    public ScheduledExecutorService scheduledExecutorService() {
        return Executors.newScheduledThreadPool(5, r -> {
            var thread = new Thread(r);
            thread.setName("worker-scheduler-%d".formatted(thread.threadId()));
            thread.setDaemon(false);
            return thread;
        });
    }

}