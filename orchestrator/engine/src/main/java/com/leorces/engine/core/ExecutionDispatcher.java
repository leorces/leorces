package com.leorces.engine.core;

import com.leorces.engine.exception.ExecutionException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class ExecutionDispatcher {

    private final Map<Class<?>, CommandHandler<?>> handlers = new HashMap<>();

    public ExecutionDispatcher(List<CommandHandler<?>> commandHandlers) {
        registerHandlers(commandHandlers);
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
        try {
            var handler = getHandler(command);
            handler.handle(command);
        } catch (Exception e) {
            throw new ExecutionException("Command dispatch failed", e);
        }
    }

    @SuppressWarnings("unchecked")
    private <T extends ExecutionCommand> CommandHandler<T> getHandler(T command) {
        var handler = (CommandHandler<T>) handlers.get(command.getClass());
        if (handler == null) {
            throw new ExecutionException("No handler found for command type: %s".formatted(command.getClass().getSimpleName()));
        }
        return handler;
    }

    private void registerHandlers(List<CommandHandler<?>> commandHandlers) {
        for (var handler : commandHandlers) {
            handlers.put(handler.getCommandType(), handler);
        }
    }

}