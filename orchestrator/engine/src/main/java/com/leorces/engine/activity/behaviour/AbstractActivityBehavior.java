package com.leorces.engine.activity.behaviour;

import com.leorces.engine.activity.command.CancelAllActivitiesCommand;
import com.leorces.engine.activity.command.CompleteActivityCommand;
import com.leorces.engine.activity.command.RunActivityCommand;
import com.leorces.engine.core.CommandDispatcher;
import com.leorces.model.definition.activity.ActivityDefinition;
import com.leorces.model.definition.activity.ActivityType;
import com.leorces.model.runtime.activity.ActivityExecution;
import com.leorces.persistence.ActivityPersistence;

import java.util.List;
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
    public ActivityExecution complete(ActivityExecution activity) {
        return activityPersistence.complete(activity);
    }

    public void cancel(ActivityExecution activity) {
        activityPersistence.cancel(activity);
    }

    public void terminate(ActivityExecution activity) {
        activityPersistence.terminate(activity);
    }

    public boolean fail(ActivityExecution activity) {
        activityPersistence.fail(activity);
        return true;
    }

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
        var activities = activityPersistence.findActive(processId, eventBasedGatewayOpt.get().outgoing());
        dispatcher.dispatch(CancelAllActivitiesCommand.of(activities));
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
