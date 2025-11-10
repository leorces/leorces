package com.leorces.engine.activity.behaviour;

import com.leorces.engine.activity.command.CompleteActivityCommand;
import com.leorces.engine.activity.command.HandleActivityCompletionWithNextActivitiesCommand;
import com.leorces.engine.activity.command.HandleActivityCompletionWithoutNextActivitiesCommand;
import com.leorces.engine.activity.command.RunActivityCommand;
import com.leorces.engine.core.CommandDispatcher;
import com.leorces.engine.variables.command.SetActivityVariablesCommand;
import com.leorces.model.definition.activity.ActivityDefinition;
import com.leorces.model.definition.activity.ActivityType;
import com.leorces.model.runtime.activity.ActivityExecution;
import com.leorces.persistence.ActivityPersistence;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public abstract class AbstractActivityBehavior implements ActivityBehavior {

    protected final ActivityPersistence activityPersistence;
    protected final CommandDispatcher dispatcher;

    protected AbstractActivityBehavior(ActivityPersistence activityPersistence,
                                       CommandDispatcher dispatcher) {
        this.activityPersistence = activityPersistence;
        this.dispatcher = dispatcher;
    }

    @Override
    public void run(ActivityExecution activity) {
        dispatcher.dispatchAsync(CompleteActivityCommand.of(activity));
    }

    @Override
    public void complete(ActivityExecution activity, Map<String, Object> variables) {
        var completedActivity = activityPersistence.complete(activity);
        postComplete(completedActivity, variables);
    }

    @Override
    public void terminate(ActivityExecution activity, boolean withInterruption) {
        var terminatedActivity = activityPersistence.terminate(activity);
        postTerminate(terminatedActivity, withInterruption);
    }

    @Override
    public boolean fail(ActivityExecution activity) {
        activityPersistence.fail(activity);
        return true;
    }

    @Override
    public void retry(ActivityExecution activity) {
        dispatcher.dispatchAsync(RunActivityCommand.of(activity));
    }

    @Override
    public List<ActivityDefinition> getNextActivities(ActivityExecution activity) {
        return activity.nextActivities();
    }

    protected void completeEventBasedGatewayActivities(ActivityExecution activity) {
        var processId = activity.processId();
        var eventBasedGatewayOpt = findEventBasedGateway(activity);
        if (eventBasedGatewayOpt.isEmpty()) {
            return;
        }
        activityPersistence.deleteAllActive(processId, eventBasedGatewayOpt.get().outgoing());
    }

    protected void postComplete(ActivityExecution completedActivity, Map<String, Object> variables) {
        dispatcher.dispatch(SetActivityVariablesCommand.of(completedActivity, variables));
        handleActivityCompletion(completedActivity, getNextActivities(completedActivity));
    }

    protected void postTerminate(ActivityExecution terminatedActivity, boolean withInterruption) {
        if (!withInterruption) {
            handleActivityCompletion(terminatedActivity, getNextActivities(terminatedActivity));
        }
    }

    protected void handleActivityCompletion(ActivityExecution activity, List<ActivityDefinition> nextActivities) {
        if (nextActivities.isEmpty()) {
            dispatcher.dispatch(HandleActivityCompletionWithoutNextActivitiesCommand.of(activity));
        } else {
            dispatcher.dispatch(HandleActivityCompletionWithNextActivitiesCommand.of(activity.process(), nextActivities));
        }
    }

    private Optional<ActivityDefinition> findEventBasedGateway(ActivityExecution activity) {
        return activity.previousActivities().stream()
                .filter(this::isEventBasedGateway)
                .findFirst();
    }

    private boolean isEventBasedGateway(ActivityDefinition activityDefinition) {
        return ActivityType.EVENT_BASED_GATEWAY.equals(activityDefinition.type());
    }

}
