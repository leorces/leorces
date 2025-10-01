package com.leorces.engine.activity.handler;

import com.leorces.engine.activity.behaviour.ActivityBehaviorResolver;
import com.leorces.engine.activity.command.CompleteActivityCommand;
import com.leorces.engine.activity.command.HandleActivityCompletionCommand;
import com.leorces.engine.activity.command.RunActivityCommand;
import com.leorces.engine.activity.command.TerminateActivityCommand;
import com.leorces.engine.core.CommandDispatcher;
import com.leorces.engine.core.CommandHandler;
import com.leorces.engine.exception.activity.ActivityNotFoundException;
import com.leorces.engine.process.command.CompleteProcessCommand;
import com.leorces.engine.process.command.ResolveProcessIncidentCommand;
import com.leorces.engine.process.command.TerminateProcessCommand;
import com.leorces.model.definition.activity.ActivityDefinition;
import com.leorces.model.definition.activity.ActivityType;
import com.leorces.model.runtime.activity.ActivityExecution;
import com.leorces.model.runtime.process.Process;
import com.leorces.model.runtime.process.ProcessState;
import com.leorces.persistence.ActivityPersistence;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class HandleActivityCompletionCommandHandler implements CommandHandler<HandleActivityCompletionCommand> {

    private final ActivityPersistence activityPersistence;
    private final ActivityBehaviorResolver behaviorResolver;
    private final CommandDispatcher dispatcher;

    @Override
    public void handle(HandleActivityCompletionCommand command) {
        var activity = command.activity();

        var behavior = behaviorResolver.resolveBehavior(activity.type());
        var nextActivities = behavior.getNextActivities(activity);

        if (nextActivities.isEmpty()) {
            handleNoNextActivities(activity);
        } else {
            handleWithNextActivities(activity, nextActivities);
        }
    }

    @Override
    public Class<HandleActivityCompletionCommand> getCommandType() {
        return HandleActivityCompletionCommand.class;
    }

    private void handleNoNextActivities(ActivityExecution activity) {
        if (activity.type() == ActivityType.ERROR_END_EVENT) {
            // Error will be handled by the CorrelateErrorCommandHandler
            return;
        }

        if (activity.type() == ActivityType.TERMINATE_END_EVENT) {
            handleTermination(activity);
            return;
        }

        if (hasParentActivity(activity)) {
            completeParentActivity(activity);
        } else if (!activity.isAsync()) {
            dispatcher.dispatchAsync(CompleteProcessCommand.of(activity.processId()));
        }
    }

    private void handleTermination(ActivityExecution activity) {
        log.debug("Handle termination of {} activity: {}", activity.type(), activity.id());
        if (!hasParentActivity(activity)) {
            terminateProcess(activity.processId());
            return;
        }

        terminateParentActivity(activity);
        terminateProcess(activity.processId());
    }

    private void handleWithNextActivities(ActivityExecution activity, List<ActivityDefinition> nextActivities) {
        resolveProcessIncidentIfNeeded(activity.process());
        runNextActivities(nextActivities, activity.process());
    }

    private void resolveProcessIncidentIfNeeded(Process process) {
        if (process.state() == ProcessState.INCIDENT) {
            dispatcher.dispatch(ResolveProcessIncidentCommand.of(process.id()));
        }
    }

    private void runNextActivities(List<ActivityDefinition> activities, Process process) {
        activities.stream()
                .map(nextActivity -> RunActivityCommand.of(process, nextActivity))
                .forEach(dispatcher::dispatchAsync);
    }

    private void completeParentActivity(ActivityExecution activity) {
        var parentActivity = getParentActivity(activity);
        dispatcher.dispatchAsync(CompleteActivityCommand.of(parentActivity));
    }

    private void terminateProcess(String processId) {
        dispatcher.dispatch(TerminateProcessCommand.of(processId));
    }

    private void terminateParentActivity(ActivityExecution activity) {
        var parentActivity = getParentActivity(activity);
        dispatcher.dispatch(TerminateActivityCommand.of(parentActivity.id()));
    }

    private ActivityExecution getParentActivity(ActivityExecution activity) {
        return activityPersistence.findByDefinitionId(activity.processId(), activity.parentDefinitionId())
                .orElseThrow(() -> ActivityNotFoundException.activityDefinitionNotFound(
                        activity.parentDefinitionId(), activity.processId()));
    }

    private boolean hasParentActivity(ActivityExecution activity) {
        return activity.parentDefinitionId() != null;
    }

}
