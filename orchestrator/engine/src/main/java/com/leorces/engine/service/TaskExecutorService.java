package com.leorces.engine.service;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Component
public class TaskExecutorService {

    private final AsyncTaskExecutor taskExecutor;

    public TaskExecutorService(@Qualifier("engineTaskExecutor") AsyncTaskExecutor taskExecutor) {
        this.taskExecutor = taskExecutor;
    }

    public void execute(Runnable task) {
        taskExecutor.execute(task);
    }

    public CompletableFuture<Void> submit(Runnable task) {
        return CompletableFuture.runAsync(task, taskExecutor);
    }

}
