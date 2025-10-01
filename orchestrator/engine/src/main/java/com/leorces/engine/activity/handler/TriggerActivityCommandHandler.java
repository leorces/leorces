package com.leorces.engine.activity.handler;

import com.leorces.engine.activity.behaviour.ActivityBehaviorResolver;
import com.leorces.engine.activity.command.TriggerActivityCommand;
import com.leorces.engine.core.CommandHandler;
import com.leorces.model.runtime.process.Process;
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

        if (!canHandle(process)) {
            log.debug("Can't trigger {} activity with definitionId: {} and processId: {}", definition.type(), definition.id(), process.id());
            return;
        }

        log.debug("Trigger {} activity with definitionId: {} and processId: {}", definition.type(), definition.id(), process.id());
        behaviorResolver.resolveTriggerableBehavior(definition.type())
                .ifPresent(behaviour -> behaviour.trigger(process, definition));
    }

    @Override
    public Class<TriggerActivityCommand> getCommandType() {
        return TriggerActivityCommand.class;
    }

    private boolean canHandle(Process process) {
        return !process.isInTerminalState();
    }

}
