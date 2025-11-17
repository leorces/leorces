package com.leorces.engine.activity.handler;

import com.leorces.engine.activity.behaviour.ActivityBehaviorResolver;
import com.leorces.engine.activity.command.FailActivityCommand;
import com.leorces.engine.core.CommandDispatcher;
import com.leorces.engine.core.CommandHandler;
import com.leorces.engine.process.command.IncidentProcessCommand;
import com.leorces.engine.service.activity.ActivityFactory;
import com.leorces.model.runtime.activity.ActivityExecution;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class FailActivityCommandHandler implements CommandHandler<FailActivityCommand> {

    private final ActivityBehaviorResolver behaviorResolver;
    private final ActivityFactory activityFactory;
    private final CommandDispatcher dispatcher;

    @Override
    public void handle(FailActivityCommand command) {
        var activity = getActivity(command).toBuilder()
                .failure(command.failure())
                .build();

        if (!canHandle(activity)) {
            log.debug("Can't fail {} activity with definitionId: {} and processId: {}", activity.type(), activity.definitionId(), activity.processId());
            return;
        }

        log.debug("Fail {} activity with definitionId: {} and processId: {}", activity.type(), activity.definitionId(), activity.processId());
        var behavior = behaviorResolver.resolveBehavior(activity.type());
        if (behavior.fail(activity)) {
            log.debug("Activity {} with definitionId: {} and processId: {} failed", activity.type(), activity.definitionId(), activity.processId());
            dispatcher.dispatchAsync(IncidentProcessCommand.of(activity.processId()));
        }
    }

    @Override
    public Class<FailActivityCommand> getCommandType() {
        return FailActivityCommand.class;
    }

    private ActivityExecution getActivity(FailActivityCommand command) {
        return command.activity() == null
                ? activityFactory.getById(command.activityId())
                : command.activity();
    }

    private boolean canHandle(ActivityExecution activity) {
        return (activity.state() == null || !activity.isInTerminalState())
                && (!activity.process().isInTerminalState() || activity.isAsync());
    }

}