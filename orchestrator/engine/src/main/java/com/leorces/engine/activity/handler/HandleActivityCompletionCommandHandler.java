package com.leorces.engine.activity.handler;

import com.leorces.engine.activity.command.CompleteActivityCommand;
import com.leorces.engine.activity.command.FindActivityCommand;
import com.leorces.engine.activity.command.HandleActivityCompletionCommand;
import com.leorces.engine.core.CommandDispatcher;
import com.leorces.engine.core.CommandHandler;
import com.leorces.engine.process.command.CompleteProcessCommand;
import com.leorces.model.runtime.activity.ActivityExecution;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class HandleActivityCompletionCommandHandler
        implements CommandHandler<HandleActivityCompletionCommand> {

    private final CommandDispatcher dispatcher;

    @Override
    public void handle(HandleActivityCompletionCommand command) {
        var activity = command.activity();

        if (!activity.hasParent()) {
            log.debug("{} activity with definitionId: {} and id: {} has no parent activity. Completing process: {}",
                    activity.type(), activity.definitionId(), activity.id(), activity.processId());
            completeProcess(activity.processId());
            return;
        }

        log.debug("{} activity with definitionId: {} and id: {} parent activity. Completing parent activity: {} in process: {}",
                activity.type(), activity.definitionId(), activity.id(), activity.parentDefinitionId(), activity.processId());
        completeParentActivity(activity);
    }

    @Override
    public Class<HandleActivityCompletionCommand> getCommandType() {
        return HandleActivityCompletionCommand.class;
    }

    private void completeProcess(String processId) {
        dispatcher.dispatch(CompleteProcessCommand.of(processId));
    }

    private void completeParentActivity(ActivityExecution activity) {
        var parent = getParentActivity(activity);
        log.debug("Completing subprocess: {} in process: {}", parent.id(), parent.processId());
        dispatcher.dispatch(CompleteActivityCommand.of(parent));
    }

    private ActivityExecution getParentActivity(ActivityExecution activity) {
        var processId = activity.processId();
        var parentDefinitionId = activity.parentDefinitionId();

        return dispatcher.execute(FindActivityCommand.byDefinitionId(processId, parentDefinitionId));
    }

}
