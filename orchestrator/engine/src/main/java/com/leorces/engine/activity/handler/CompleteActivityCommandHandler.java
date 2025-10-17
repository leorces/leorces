package com.leorces.engine.activity.handler;

import com.leorces.engine.activity.ActivityFactory;
import com.leorces.engine.activity.behaviour.ActivityBehaviorResolver;
import com.leorces.engine.activity.command.CompleteActivityCommand;
import com.leorces.engine.activity.command.FailActivityCommand;
import com.leorces.engine.activity.command.HandleActivityCompletionCommand;
import com.leorces.engine.core.CommandDispatcher;
import com.leorces.engine.core.CommandHandler;
import com.leorces.engine.exception.ExecutionException;
import com.leorces.engine.variables.VariablesService;
import com.leorces.engine.variables.command.SetVariablesCommand;
import com.leorces.model.runtime.activity.ActivityExecution;
import com.leorces.model.runtime.activity.ActivityFailure;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class CompleteActivityCommandHandler implements CommandHandler<CompleteActivityCommand> {

    private final ActivityBehaviorResolver behaviorResolver;
    private final VariablesService variablesService;
    private final ActivityFactory activityFactory;
    private final CommandDispatcher dispatcher;

    @Override
    public void handle(CompleteActivityCommand command) {
        var activity = getActivity(command);

        if (!canHandle(activity)) {
            log.debug("Can't complete {} activity with definitionId: {} and processId: {}", activity.type(), activity.definitionId(), activity.processId());
            return;
        }

        log.debug("Complete {} activity with definitionId: {} and processId: {}", activity.type(), activity.definitionId(), activity.processId());
        var completedActivity = completeActivity(activity);
        processVariables(completedActivity, command.variables());
        dispatcher.dispatchAsync(HandleActivityCompletionCommand.of(completedActivity));
        log.debug("Activity {} with definitionId: {} and processId: {} completed", activity.type(), activity.definitionId(), activity.processId());
    }

    @Override
    public Class<CompleteActivityCommand> getCommandType() {
        return CompleteActivityCommand.class;
    }

    private ActivityExecution completeActivity(ActivityExecution activity) {
        try {
            var behavior = behaviorResolver.resolveBehavior(activity.type());
            return behavior.complete(activity);
        } catch (Exception e) {
            dispatcher.dispatch(FailActivityCommand.of(activity, ActivityFailure.of(e)));
            throw new ExecutionException("Activity completion failed", e);
        }
    }

    private void processVariables(ActivityExecution activity, Map<String, Object> variables) {
        var outputVariables = variablesService.evaluate(activity, activity.outputs());
        var outputVariablesMap = variablesService.toMap(outputVariables);
        var combinedVariables = combineVariables(variables, outputVariablesMap);
        dispatcher.dispatch(SetVariablesCommand.of(activity.process(), combinedVariables));
    }

    private Map<String, Object> combineVariables(Map<String, Object> inputVariables, Map<String, Object> outputVariables) {
        var combined = new HashMap<>(inputVariables);
        combined.putAll(outputVariables);
        return combined;
    }

    private ActivityExecution getActivity(CompleteActivityCommand command) {
        return command.activity() == null
                ? activityFactory.getById(command.activityId())
                : command.activity();
    }

    private boolean canHandle(ActivityExecution activity) {
        return activity.state() == null || !activity.isInTerminalState() || !activity.process().isInTerminalState();
    }

}