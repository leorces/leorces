package com.leorces.engine.activity.behaviour.subprocess;

import com.leorces.engine.activity.behaviour.AbstractActivityBehavior;
import com.leorces.engine.activity.command.RetryAllActivitiesCommand;
import com.leorces.engine.core.CommandDispatcher;
import com.leorces.engine.process.command.CancelProcessCommand;
import com.leorces.engine.process.command.RunProcessCommand;
import com.leorces.engine.process.command.TerminateProcessCommand;
import com.leorces.model.definition.activity.ActivityType;
import com.leorces.model.runtime.activity.ActivityExecution;
import com.leorces.persistence.ActivityPersistence;
import org.springframework.stereotype.Component;

@Component
public class CallActivityBehavior extends AbstractActivityBehavior {

    protected CallActivityBehavior(ActivityPersistence activityPersistence,
                                   CommandDispatcher dispatcher) {
        super(activityPersistence, dispatcher);
    }

    @Override
    public void run(ActivityExecution activity) {
        var result = activityPersistence.run(activity);
        dispatcher.dispatch(RunProcessCommand.of(result));
    }

    @Override
    public void retry(ActivityExecution activity) {
        var failedActivities = activityPersistence.findFailed(activity.id());
        dispatcher.dispatchAsync(RetryAllActivitiesCommand.of(failedActivities));
    }

    @Override
    public void cancel(ActivityExecution activity) {
        dispatcher.dispatch(CancelProcessCommand.of(activity.id()));
        activityPersistence.cancel(activity);
    }

    @Override
    public void terminate(ActivityExecution activity) {
        dispatcher.dispatch(TerminateProcessCommand.of(activity.id()));
        activityPersistence.terminate(activity);
    }

    @Override
    public ActivityType type() {
        return ActivityType.CALL_ACTIVITY;
    }

}
