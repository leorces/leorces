package com.leorces.engine.core;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CommandDispatcher {

    private final ApplicationEventPublisher publisher;

    public void dispatch(ExecutionCommand command) {
        publisher.publishEvent(command);
    }

    public void dispatchAsync(ExecutionCommand command) {
        publisher.publishEvent(new AsyncExecutionCommandWrapper(command));
    }

}
