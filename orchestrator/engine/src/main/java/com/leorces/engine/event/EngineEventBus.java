package com.leorces.engine.event;


import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
public class EngineEventBus {

    private final ApplicationEventPublisher publisher;

    public void publish(ApplicationEvent event) {
        publisher.publishEvent(event);
    }

}
