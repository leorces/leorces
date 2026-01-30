package com.leorces.engine.activity.behaviour.subprocess;

import com.leorces.engine.activity.behaviour.AbstractActivityBehavior;
import com.leorces.engine.activity.command.GetCallActivityMappingsCommand;
import com.leorces.engine.activity.command.RetryAllActivitiesCommand;
import com.leorces.engine.core.CommandDispatcher;
import com.leorces.engine.process.command.DeleteProcessCommand;
import com.leorces.engine.process.command.RunProcessCommand;
import com.leorces.engine.process.command.TerminateProcessCommand;
import com.leorces.model.definition.activity.ActivityType;
import com.leorces.model.runtime.activity.ActivityExecution;
import com.leorces.persistence.ActivityPersistence;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class CallActivityBehavior extends AbstractActivityBehavior {

    protected CallActivityBehavior(ActivityPersistence activityPersistence,
                                   CommandDispatcher dispatcher) {
        super(activityPersistence, dispatcher);
    }

    @Override
    public void run(ActivityExecution callActivity) {
        var newCallActivity = activityPersistence.run(callActivity);
        dispatcher.dispatch(RunProcessCommand.byCallActivity(newCallActivity));
    }

    @Override
    public void complete(ActivityExecution activity, Map<String, Object> variables) {
        var completedCallActivity = activityPersistence.complete(activity);
        var outputVariables = combineVariables(
                dispatcher.execute(GetCallActivityMappingsCommand.output(activity)),
                variables
        );
        postComplete(completedCallActivity, outputVariables);
    }

    @Override
    public void retry(ActivityExecution callActivity) {
        var failedActivities = activityPersistence.findFailed(callActivity.id());
        dispatcher.dispatchAsync(RetryAllActivitiesCommand.of(failedActivities));
    }

    @Override
    public void terminate(ActivityExecution callActivity, boolean withInterruption) {
        if (!callActivity.process().isInTerminalState()) {
            dispatcher.dispatch(TerminateProcessCommand.of(callActivity.id(), false));
        }

        var terminatedCallActivity = activityPersistence.terminate(callActivity);
        postTerminate(terminatedCallActivity, withInterruption);
    }

    @Override
    public void delete(ActivityExecution callActivity) {
        if (!callActivity.process().isInTerminalState()) {
            dispatcher.dispatch(DeleteProcessCommand.of(callActivity.id(), false));
        }

        activityPersistence.delete(callActivity);
    }

    @Override
    public ActivityType type() {
        return ActivityType.CALL_ACTIVITY;
    }

    private Map<String, Object> combineVariables(Map<String, Object> inputVariables, Map<String, Object> outputVariables) {
        var combined = new HashMap<>(inputVariables);
        combined.putAll(outputVariables);
        return combined;
    }

}
