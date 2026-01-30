package com.leorces.engine.activity.handler;

import com.leorces.api.exception.ExecutionException;
import com.leorces.engine.activity.behaviour.ActivityBehaviorResolver;
import com.leorces.engine.activity.command.CompleteActivityCommand;
import com.leorces.engine.activity.command.FailActivityCommand;
import com.leorces.engine.activity.command.FindActivityCommand;
import com.leorces.engine.core.CommandDispatcher;
import com.leorces.engine.core.CommandHandler;
import com.leorces.model.runtime.activity.ActivityExecution;
import com.leorces.model.runtime.activity.ActivityFailure;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CompleteActivityCommandHandler implements CommandHandler<CompleteActivityCommand> {

    private final ActivityBehaviorResolver behaviorResolver;
    private final CommandDispatcher dispatcher;

    @Override
    public void handle(CompleteActivityCommand command) {
        var activity = getActivity(command);

        if (!canHandle(activity)) {
            log.debug("Can't complete {} activity with definitionId: {} and processId: {}", activity.type(), activity.definitionId(), activity.processId());
            return;
        }

        log.debug("Complete {} activity with definitionId: {} and processId: {}", activity.type(), activity.definitionId(), activity.processId());
        try {
            behaviorResolver.resolveBehavior(activity.type()).complete(activity, command.variables());
        } catch (Exception e) {
            dispatcher.dispatch(FailActivityCommand.of(activity, ActivityFailure.of(e)));
            throw ExecutionException.of("Can't complete activity", activity, e);
        }
    }

    @Override
    public Class<CompleteActivityCommand> getCommandType() {
        return CompleteActivityCommand.class;
    }

    private ActivityExecution getActivity(CompleteActivityCommand command) {
        return command.activity() == null
                ? dispatcher.execute(FindActivityCommand.byId(command.activityId()))
                : command.activity();
    }

    private boolean canHandle(ActivityExecution activity) {
        return (activity.state() == null || !activity.isInTerminalState())
                && (!activity.process().isInTerminalState() || activity.isAsync());
    }

}