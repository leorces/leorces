package com.leorces.engine.activity.handler;

import com.leorces.engine.activity.command.CompleteActivityCommand;
import com.leorces.engine.activity.command.HandleActivityCompletionWithoutNextActivitiesCommand;
import com.leorces.engine.activity.command.TerminateActivityCommand;
import com.leorces.engine.core.CommandDispatcher;
import com.leorces.engine.core.CommandHandler;
import com.leorces.engine.exception.activity.ActivityNotFoundException;
import com.leorces.engine.process.command.CompleteProcessCommand;
import com.leorces.engine.process.command.TerminateProcessCommand;
import com.leorces.model.definition.activity.ActivityType;
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

        if (activity.type() == ActivityType.ERROR_END_EVENT) {
            // Error will be handled by the CorrelateErrorCommandHandler
            return;
        }

        if (activity.type() == ActivityType.TERMINATE_END_EVENT) {
            handleTermination(activity);
            return;
        }

        if (activity.hasParent() && !activity.isAsync()) {
            completeParentActivity(activity);
        } else if (!activity.isAsync()) {
            dispatcher.dispatchAsync(CompleteProcessCommand.of(activity.process()));
        }
    }

    @Override
    public Class<HandleActivityCompletionWithoutNextActivitiesCommand> getCommandType() {
        return HandleActivityCompletionWithoutNextActivitiesCommand.class;
    }

    private void handleTermination(ActivityExecution activity) {
        log.debug("Handle termination of {} activity: {}", activity.type(), activity.id());
        if (!activity.hasParent()) {
            terminateProcess(activity.processId());
            return;
        }

        terminateParentActivity(activity);
        terminateProcess(activity.processId());
    }

    private void terminateProcess(String processId) {
        dispatcher.dispatch(TerminateProcessCommand.of(processId));
    }

    private void terminateParentActivity(ActivityExecution activity) {
        var parentActivity = getParentActivity(activity);

        if (parentActivity.type().isEventSubprocess() && parentActivity.hasParent()) {
            dispatcher.dispatch(TerminateActivityCommand.of(parentActivity, false));
            dispatcher.dispatch(TerminateActivityCommand.of(activity.processId(), parentActivity.parentDefinitionId(), false));
            return;
        }

        dispatcher.dispatch(TerminateActivityCommand.of(parentActivity.id()));
    }

    private void completeParentActivity(ActivityExecution activity) {
        var parentActivity = getParentActivity(activity);
        dispatcher.dispatchAsync(CompleteActivityCommand.of(parentActivity));
    }

    private ActivityExecution getParentActivity(ActivityExecution activity) {
        return activityPersistence.findByDefinitionId(activity.processId(), activity.parentDefinitionId())
                .orElseThrow(() -> ActivityNotFoundException.activityDefinitionNotFound(
                        activity.parentDefinitionId(), activity.processId()));
    }

}
