package com.leorces.engine.activity.handler;

import com.leorces.engine.activity.command.CompleteActivityCommand;
import com.leorces.engine.activity.command.HandleActivityCompletionWithoutNextActivitiesCommand;
import com.leorces.engine.core.CommandDispatcher;
import com.leorces.engine.core.CommandHandler;
import com.leorces.engine.exception.activity.ActivityNotFoundException;
import com.leorces.engine.process.command.CompleteProcessCommand;
import com.leorces.model.runtime.activity.ActivityExecution;
import com.leorces.persistence.ActivityPersistence;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class HandleActivityCompletionWithoutNextActivitiesCommandHandler
        implements CommandHandler<HandleActivityCompletionWithoutNextActivitiesCommand> {

    private final ActivityPersistence activityPersistence;
    private final CommandDispatcher dispatcher;

    @Override
    public void handle(HandleActivityCompletionWithoutNextActivitiesCommand command) {
        var activity = command.activity();

        if (!activity.hasParent()) {
            completeProcess(activity.processId());
            return;
        }

        completeSubprocess(activity);
    }

    @Override
    public Class<HandleActivityCompletionWithoutNextActivitiesCommand> getCommandType() {
        return HandleActivityCompletionWithoutNextActivitiesCommand.class;
    }

    private void completeProcess(String processId) {
        dispatcher.dispatch(CompleteProcessCommand.of(processId));
    }

    private void completeSubprocess(ActivityExecution activity) {
        var parentActivity = getParentActivity(activity);
        dispatcher.dispatchAsync(CompleteActivityCommand.of(parentActivity));
    }

    private ActivityExecution getParentActivity(ActivityExecution activity) {
        return activityPersistence.findByDefinitionId(activity.processId(), activity.parentDefinitionId())
                .orElseThrow(() -> ActivityNotFoundException.activityDefinitionNotFound(
                        activity.parentDefinitionId(), activity.processId()));
    }

}
