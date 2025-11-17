package com.leorces.engine.activity.handler;

import com.leorces.engine.activity.behaviour.ActivityBehaviorResolver;
import com.leorces.engine.activity.command.TerminateActivityCommand;
import com.leorces.engine.core.CommandHandler;
import com.leorces.engine.service.activity.ActivityFactory;
import com.leorces.model.runtime.activity.ActivityExecution;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TerminateActivityCommandHandler implements CommandHandler<TerminateActivityCommand> {

    private final ActivityBehaviorResolver behaviorResolver;
    private final ActivityFactory activityFactory;

    @Override
    public void handle(TerminateActivityCommand command) {
        var activity = getActivity(command);

        if (!canHandle(activity)) {
            log.debug("Can't terminate {} activity with definitionId: {} and processId: {}", activity.type(), activity.definitionId(), activity.processId());
            return;
        }

        log.debug("Terminate {} activity with definitionId: {} and processId: {}", activity.type(), activity.definitionId(), activity.processId());
        behaviorResolver.resolveBehavior(activity.type()).terminate(activity, command.withInterruption());
        log.debug("Activity {} with definitionId: {} and processId: {} terminated", activity.type(), activity.definitionId(), activity.processId());
    }

    @Override
    public Class<TerminateActivityCommand> getCommandType() {
        return TerminateActivityCommand.class;
    }

    private ActivityExecution getActivity(TerminateActivityCommand command) {
        if (command.activity() != null) {
            return command.activity();
        }

        if (command.activityId() != null) {
            return activityFactory.getById(command.activityId());
        }

        return activityFactory.getByDefinitionId(command.definitionId(), command.processId());
    }

    private boolean canHandle(ActivityExecution activity) {
        return !activity.isInTerminalState()
                && (!activity.process().isInTerminalState() || activity.isAsync());
    }

}
