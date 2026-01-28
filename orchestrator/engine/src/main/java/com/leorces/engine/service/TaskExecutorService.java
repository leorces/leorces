package com.leorces.engine.service;

import com.leorces.api.exception.ExecutionException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.stereotype.Component;

import java.util.concurrent.Callable;
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

    public CompletableFuture<Void> runAsync(Runnable task) {
        return CompletableFuture.runAsync(task, taskExecutor);
    }

    public <T> CompletableFuture<T> supplyAsync(Callable<T> task) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return task.call();
            } catch (Exception ex) {
                throw ExecutionException.of(ex);
            }
        }, taskExecutor);
    }

}
