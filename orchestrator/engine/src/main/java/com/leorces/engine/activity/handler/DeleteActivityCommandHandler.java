package com.leorces.engine.activity.handler;

import com.leorces.engine.activity.behaviour.ActivityBehaviorResolver;
import com.leorces.engine.activity.command.DeleteActivityCommand;
import com.leorces.engine.core.CommandHandler;
import com.leorces.engine.service.activity.ActivityFactory;
import com.leorces.model.runtime.activity.ActivityExecution;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DeleteActivityCommandHandler implements CommandHandler<DeleteActivityCommand> {

    private final ActivityBehaviorResolver behaviorResolver;
    private final ActivityFactory activityFactory;

    @Override
    public void handle(DeleteActivityCommand command) {
        var activity = getActivity(command);

        if (!canHandle(activity)) {
            log.debug("Can't delete {} activity with definitionId: {} and processId: {}", activity.type(), activity.definitionId(), activity.processId());
            return;
        }

        log.debug("Delete {} activity with definitionId: {} and processId: {}", activity.type(), activity.definitionId(), activity.processId());
        behaviorResolver.resolveBehavior(activity.type()).delete(activity);
    }

    @Override
    public Class<DeleteActivityCommand> getCommandType() {
        return DeleteActivityCommand.class;
    }

    private ActivityExecution getActivity(DeleteActivityCommand command) {
        return activityFactory.getById(command.activityId());
    }

    private boolean canHandle(ActivityExecution activity) {
        return !activity.isInTerminalState()
                && (!activity.process().isInTerminalState() || activity.isAsync());
    }

}
