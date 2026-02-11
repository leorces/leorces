package com.leorces.engine.activity.handler;

import com.leorces.engine.activity.behaviour.ActivityBehaviorResolver;
import com.leorces.engine.activity.command.DeleteActivityCommand;
import com.leorces.engine.activity.command.FindActivityCommand;
import com.leorces.engine.core.CommandDispatcher;
import com.leorces.engine.core.CommandHandler;
import com.leorces.model.runtime.activity.ActivityExecution;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DeleteActivityCommandHandler implements CommandHandler<DeleteActivityCommand> {

    private final ActivityBehaviorResolver behaviorResolver;
    private final CommandDispatcher dispatcher;

    @Override
    public void handle(DeleteActivityCommand command) {
        var activity = getActivity(command);
        log.debug("Delete {} activity with definitionId: {} and processId: {}", activity.type(), activity.definitionId(), activity.processId());
        behaviorResolver.resolveBehavior(activity.type()).delete(activity);
    }

    @Override
    public Class<DeleteActivityCommand> getCommandType() {
        return DeleteActivityCommand.class;
    }

    private ActivityExecution getActivity(DeleteActivityCommand command) {
        return command.activity() == null
                ? dispatcher.execute(FindActivityCommand.byId(command.activityId()))
                : command.activity();
    }

}
