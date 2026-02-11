package com.leorces.engine.activity.behaviour.subprocess;

import com.leorces.engine.activity.behaviour.AbstractActivityBehavior;
import com.leorces.engine.activity.command.GetCallActivityMappingsCommand;
import com.leorces.engine.activity.command.RetryAllActivitiesCommand;
import com.leorces.engine.core.CommandDispatcher;
import com.leorces.engine.process.command.CreateProcessByCallActivityCommand;
import com.leorces.engine.process.command.DeleteProcessCommand;
import com.leorces.engine.process.command.RunProcessCommand;
import com.leorces.engine.process.command.TerminateProcessCommand;
import com.leorces.engine.variables.command.GetScopedVariablesCommand;
import com.leorces.juel.ExpressionEvaluator;
import com.leorces.model.definition.activity.ActivityType;
import com.leorces.model.definition.activity.subprocess.CallActivity;
import com.leorces.model.runtime.activity.ActivityExecution;
import com.leorces.persistence.ActivityPersistence;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class CallActivityBehavior extends AbstractActivityBehavior {

    private final ExpressionEvaluator expressionEvaluator;

    protected CallActivityBehavior(ActivityPersistence activityPersistence,
                                   CommandDispatcher dispatcher,
                                   ExpressionEvaluator expressionEvaluator) {
        super(activityPersistence, dispatcher);
        this.expressionEvaluator = expressionEvaluator;
    }

    @Override
    public void run(ActivityExecution callActivity) {
        var definition = (CallActivity) callActivity.definition();

        if (definition.isMultiInstance()) {
            runMultiInstance(callActivity, definition);
        } else {
            runSingleInstance(callActivity);
        }
    }

    @Override
    public void complete(ActivityExecution callActivity, Map<String, Object> variables) {
        var definition = (CallActivity) callActivity.definition();
        var completedCallActivity = activityPersistence.complete(callActivity);
        var outputVariables = getOutputVariables(callActivity, variables);

        if (!definition.isMultiInstance() || areAllInstancesCompleted(callActivity, definition)) {
            postComplete(completedCallActivity, outputVariables);
        }
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

    private void runSingleInstance(ActivityExecution callActivity) {
        var newCallActivity = activityPersistence.run(callActivity);
        dispatcher.dispatch(RunProcessCommand.byCallActivity(newCallActivity));
    }

    private void runMultiInstance(ActivityExecution callActivity, CallActivity definition) {
        var loopCharacteristics = definition.multiInstanceLoopCharacteristics();
        var scopedVariables = callActivity.getScopedVariables(() -> dispatcher.execute(GetScopedVariablesCommand.of(callActivity)));
        var collection = expressionEvaluator.evaluate(loopCharacteristics.collection(), scopedVariables, List.class);
        var elementVariable = loopCharacteristics.elementVariable();
        for (var element : collection) {
            var newCallActivity = activityPersistence.run(callActivity);
            var processToRun = dispatcher.execute(
                    CreateProcessByCallActivityCommand.of(newCallActivity, Map.of(elementVariable, element))
            );
            dispatcher.dispatch(RunProcessCommand.of(processToRun));
        }
    }

    private Map<String, Object> getOutputVariables(ActivityExecution callActivity, Map<String, Object> variables) {
        return combineVariables(
                dispatcher.execute(GetCallActivityMappingsCommand.output(callActivity)),
                variables
        );
    }

    private boolean areAllInstancesCompleted(ActivityExecution callActivity, CallActivity definition) {
        return activityPersistence.isAllCompleted(callActivity.processId(), List.of(definition.id()));
    }

    private Map<String, Object> combineVariables(Map<String, Object> inputVariables, Map<String, Object> outputVariables) {
        var combined = new HashMap<>(inputVariables);
        combined.putAll(outputVariables);
        return combined;
    }

}
