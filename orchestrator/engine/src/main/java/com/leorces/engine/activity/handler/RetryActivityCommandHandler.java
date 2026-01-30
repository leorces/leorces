package com.leorces.engine.activity.handler;

import com.leorces.engine.activity.behaviour.ActivityBehaviorResolver;
import com.leorces.engine.activity.command.FindActivityCommand;
import com.leorces.engine.activity.command.RetryActivityCommand;
import com.leorces.engine.core.CommandDispatcher;
import com.leorces.engine.core.CommandHandler;
import com.leorces.model.runtime.activity.ActivityExecution;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RetryActivityCommandHandler implements CommandHandler<RetryActivityCommand> {

    private final ActivityBehaviorResolver behaviorResolver;
    private final CommandDispatcher dispatcher;

    @Override
    public void handle(RetryActivityCommand command) {
        var activity = getActivity(command);

        if (!canHandle(activity)) {
            log.debug("Can't retry {} activity with definitionId: {} and processId: {}", activity.type(), activity.definitionId(), activity.processId());
            return;
        }

        log.debug("Retry {} activity with definitionId: {} and processId: {}", activity.type(), activity.definitionId(), activity.processId());
        behaviorResolver.resolveBehavior(activity.type()).retry(activity);
    }

    @Override
    public Class<RetryActivityCommand> getCommandType() {
        return RetryActivityCommand.class;
    }

    private ActivityExecution getActivity(RetryActivityCommand command) {
        return command.activity() == null
                ? dispatcher.execute(FindActivityCommand.byId(command.activityId()))
                : command.activity();
    }

    private boolean canHandle(ActivityExecution activity) {
        return !activity.isInTerminalState()
                && (!activity.process().isInTerminalState() || activity.isAsync());
    }

}