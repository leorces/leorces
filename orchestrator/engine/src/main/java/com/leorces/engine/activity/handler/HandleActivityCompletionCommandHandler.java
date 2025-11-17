package com.leorces.engine.activity.handler;

import com.leorces.engine.activity.command.CompleteActivityCommand;
import com.leorces.engine.activity.command.HandleActivityCompletionCommand;
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
public class HandleActivityCompletionCommandHandler
        implements CommandHandler<HandleActivityCompletionCommand> {

    private final ActivityPersistence activityPersistence;
    private final CommandDispatcher dispatcher;

    @Override
    public void handle(HandleActivityCompletionCommand command) {
        var activity = command.activity();

        if (!activity.hasParent()) {
            completeProcess(activity.processId());
            return;
        }

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

        if (parent.type().isEventSubprocess()) {
            completeEventSubprocess(parent);
        } else {
            dispatcher.dispatch(CompleteActivityCommand.of(parent));
        }
    }

    private void completeEventSubprocess(ActivityExecution eventSubprocess) {
        dispatcher.dispatch(CompleteActivityCommand.of(eventSubprocess));

        var process = eventSubprocess.process();

        if (!eventSubprocess.hasParent()) {
            completeProcess(process.id());
            return;
        }

        var parent = getParentActivity(eventSubprocess);
        dispatcher.dispatch(CompleteActivityCommand.of(parent));
    }

    private ActivityExecution getParentActivity(ActivityExecution activity) {
        var processId = activity.processId();
        var parentDefinitionId = activity.parentDefinitionId();

        return activityPersistence.findByDefinitionId(processId, parentDefinitionId)
                .orElseThrow(() ->
                        ActivityNotFoundException.activityDefinitionNotFound(parentDefinitionId, processId)
                );
    }

}
