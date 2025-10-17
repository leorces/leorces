package com.leorces.engine.activity.behaviour.subprocess;

import com.leorces.engine.activity.behaviour.AbstractActivityBehavior;
import com.leorces.engine.activity.command.RetryAllActivitiesCommand;
import com.leorces.engine.core.CommandDispatcher;
import com.leorces.engine.process.command.RunProcessCommand;
import com.leorces.engine.process.command.TerminateProcessCommand;
import com.leorces.engine.service.CallActivityService;
import com.leorces.engine.variables.command.SetVariablesCommand;
import com.leorces.model.definition.activity.ActivityType;
import com.leorces.model.runtime.activity.ActivityExecution;
import com.leorces.persistence.ActivityPersistence;
import org.springframework.stereotype.Component;

@Component
public class CallActivityBehavior extends AbstractActivityBehavior {

    private final CallActivityService callActivityService;

    protected CallActivityBehavior(ActivityPersistence activityPersistence,
                                   CommandDispatcher dispatcher,
                                   CallActivityService callActivityService) {
        super(activityPersistence, dispatcher);
        this.callActivityService = callActivityService;
    }

    @Override
    public void run(ActivityExecution activity) {
        var result = activityPersistence.run(activity);
        dispatcher.dispatch(RunProcessCommand.of(result));
    }

    @Override
    public ActivityExecution complete(ActivityExecution activity) {
        var result = activityPersistence.complete(activity);
        processOutputMappings(activity);
        return result;
    }

    @Override
    public void retry(ActivityExecution activity) {
        var failedActivities = activityPersistence.findFailed(activity.id());
        dispatcher.dispatchAsync(RetryAllActivitiesCommand.of(failedActivities));
    }

    @Override
    public ActivityExecution terminate(ActivityExecution activity) {
        if (!activity.process().isInTerminalState()) {
            dispatcher.dispatch(TerminateProcessCommand.of(activity.id(), false));
        }
        return activityPersistence.terminate(activity);
    }

    @Override
    public ActivityType type() {
        return ActivityType.CALL_ACTIVITY;
    }

    private void processOutputMappings(ActivityExecution activity) {
        var variables = callActivityService.getOutputMappings(activity);
        dispatcher.dispatch(SetVariablesCommand.of(activity.process(), variables));
    }

}
