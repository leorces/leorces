package com.leorces.engine.core;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
@RequiredArgsConstructor
public class CommandDispatcher {

    private final ApplicationEventPublisher publisher;
    private final ExecutionDispatcher executionDispatcher;

    public void dispatch(ExecutionCommand command) {
        publisher.publishEvent(command);
    }

    public void dispatchAsync(ExecutionCommand command) {
        publisher.publishEvent(new AsyncExecutionCommandWrapper(command));
    }

    /**
     * Executes a command and returns the result.
     *
     * @param command the command to execute
     * @param <R>     the result type
     * @param <T>     the command type
     * @return the execution result
     */
    public <R, T extends ExecutionResultCommand<R>> R execute(T command) {
        Objects.requireNonNull(command);
        return executeCommand(command);
    }

    private <R, T extends ExecutionResultCommand<R>> R executeCommand(T command) {
        return executionDispatcher.execute(command);
    }

}
