package com.leorces.engine.activity;

import com.leorces.engine.activity.behaviour.ActivityBehaviorResolver;
import com.leorces.engine.event.activity.trigger.TriggerActivityByDefinitionEventAsync;
import com.leorces.model.definition.activity.ActivityDefinition;
import com.leorces.model.runtime.process.Process;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
class ActivityTriggerService {

    private final ActivityBehaviorResolver behaviorResolver;

    @Async
    @EventListener
    void handleTrigger(TriggerActivityByDefinitionEventAsync event) {
        trigger(event.definition, event.process);
    }

    private void trigger(ActivityDefinition definition, Process process) {
        log.debug("Trigger {} activity with definitionId: {} and processId: {}", definition.type(), definition.id(), process.id());
        behaviorResolver.resolveTriggerableStrategy(definition.type())
                .ifPresent(behaviour -> behaviour.trigger(process, definition));
    }

}
