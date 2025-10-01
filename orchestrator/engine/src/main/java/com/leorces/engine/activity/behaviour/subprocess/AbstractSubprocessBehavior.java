package com.leorces.engine.activity.behaviour.subprocess;

import com.leorces.engine.activity.behaviour.AbstractActivityBehavior;
import com.leorces.engine.activity.command.CancelAllActivitiesCommand;
import com.leorces.engine.activity.command.RunActivityCommand;
import com.leorces.engine.activity.command.TerminateAllActivitiesCommand;
import com.leorces.engine.core.CommandDispatcher;
import com.leorces.model.definition.activity.ActivityDefinition;
import com.leorces.model.runtime.activity.ActivityExecution;
import com.leorces.persistence.ActivityPersistence;

import java.util.List;

public abstract class AbstractSubprocessBehavior extends AbstractActivityBehavior {

    protected AbstractSubprocessBehavior(ActivityPersistence activityPersistence,
                                         CommandDispatcher dispatcher) {
        super(activityPersistence, dispatcher);
    }

    @Override
    public void run(ActivityExecution activity) {
        var result = activityPersistence.run(activity);
        dispatcher.dispatchAsync(RunActivityCommand.of(result.process(), getStartEvent(result)));
    }

    @Override
    public void cancel(ActivityExecution activity) {
        cancelChildActivities(activity);
        activityPersistence.cancel(activity);
    }

    @Override
    public void terminate(ActivityExecution activity) {
        terminateChildActivities(activity);
        activityPersistence.terminate(activity);
    }

    protected abstract ActivityDefinition getStartEvent(ActivityExecution activity);

    private void cancelChildActivities(ActivityExecution activity) {
        var childActivityIds = getChildActivityIds(activity);
        var childActivities = activityPersistence.findActive(activity.processId(), childActivityIds);
        dispatcher.dispatch(CancelAllActivitiesCommand.of(childActivities));
    }

    private void terminateChildActivities(ActivityExecution activity) {
        var childActivityIds = getChildActivityIds(activity);
        var childActivities = activityPersistence.findActive(activity.processId(), childActivityIds);
        dispatcher.dispatch(TerminateAllActivitiesCommand.of(childActivities));
    }

    private List<String> getChildActivityIds(ActivityExecution activity) {
        return activity.childActivities().stream()
                .map(ActivityDefinition::id)
                .toList();
    }

}
