package com.leorces.engine.core;

import com.leorces.api.exception.ExecutionException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Slf4j
@Component
public class ExecutionDispatcher {

    private final Map<Class<?>, CommandHandler<?>> handlers = new HashMap<>();
    private final ObjectProvider<List<CommandHandler<?>>> handlersProvider;
    private boolean initialized = false;

    public ExecutionDispatcher(ObjectProvider<List<CommandHandler<?>>> handlersProvider) {
        this.handlersProvider = handlersProvider;
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
        return performExecution(command);
    }

    @EventListener
    void handle(ExecutionCommand command) {
        dispatch(command);
    }

    @Async
    @EventListener
    void handle(AsyncExecutionCommandWrapper wrapper) {
        dispatch(wrapper.command());
    }

    private <T extends ExecutionCommand> void dispatch(T command) {
        performExecution(command);
    }

    private <R, T extends ExecutionCommand> R performExecution(T command) {
        try {
            var handler = getHandler(command);
            return handleWithResult(handler, command);
        } catch (ExecutionException e) {
            throw e;
        } catch (Exception e) {
            log.error("Command execution failed", e);
            throw ExecutionException.of("Execution failed", e);
        }
    }

    @SuppressWarnings("unchecked")
    private <R, T extends ExecutionCommand> R handleWithResult(CommandHandler<T> handler, T command) {
        if (handler instanceof ResultCommandHandler<?, ?> resultHandler) {
            return ((ResultCommandHandler<T, R>) resultHandler).execute(command);
        }
        handler.handle(command);
        return null;
    }

    @SuppressWarnings("unchecked")
    private <T extends ExecutionCommand> CommandHandler<T> getHandler(T command) {
        ensureHandlersInitialized();
        var handler = (CommandHandler<T>) handlers.get(command.getClass());
        if (handler == null) {
            log.error("No handler found for command type: {}", command.getClass().getSimpleName());
            throw ExecutionException.of("No handler found for command type: %s".formatted(command.getClass().getSimpleName()));
        }
        return handler;
    }

    private synchronized void ensureHandlersInitialized() {
        if (!initialized) {
            handlersProvider.ifAvailable(this::registerHandlers);
            initialized = true;
        }
    }

    private void registerHandlers(List<CommandHandler<?>> commandHandlers) {
        for (var handler : commandHandlers) {
            handlers.put(handler.getCommandType(), handler);
        }
    }

}