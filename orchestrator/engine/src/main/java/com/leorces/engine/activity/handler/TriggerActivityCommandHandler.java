package com.leorces.engine.activity.handler;

import com.leorces.engine.activity.behaviour.ActivityBehaviorResolver;
import com.leorces.engine.activity.command.TriggerActivityCommand;
import com.leorces.engine.core.CommandHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TriggerActivityCommandHandler implements CommandHandler<TriggerActivityCommand> {

    private final ActivityBehaviorResolver behaviorResolver;

    @Override
    public void handle(TriggerActivityCommand command) {
        var definition = command.definition();
        var process = command.process();

        log.debug("Trigger {} activity with definitionId: {} and processId: {}", definition.type(), definition.id(), process.id());
        behaviorResolver.resolveTriggerableBehavior(definition.type())
                .ifPresent(behaviour -> behaviour.trigger(process, definition));
    }

    @Override
    public Class<TriggerActivityCommand> getCommandType() {
        return TriggerActivityCommand.class;
    }

}
