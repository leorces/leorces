package com.leorces.engine.activity.handler;

import com.leorces.engine.activity.behaviour.ActivityBehaviorResolver;
import com.leorces.engine.activity.command.RunActivityCommand;
import com.leorces.engine.core.CommandHandler;
import com.leorces.engine.service.activity.ActivityFactory;
import com.leorces.engine.service.variable.VariablesService;
import com.leorces.model.runtime.activity.ActivityExecution;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RunActivityCommandHandler implements CommandHandler<RunActivityCommand> {

    private final VariablesService variablesService;
    private final ActivityBehaviorResolver behaviorResolver;
    private final ActivityFactory activityFactory;

    @Override
    public void handle(RunActivityCommand command) {
        var activity = getActivity(command);

        if (!canHandle(activity)) {
            log.debug("Can't run {} activity with definitionId: {} and processId: {}", activity.type(), activity.definitionId(), activity.processId());
            return;
        }

        log.debug("Run {} activity with definitionId: {} and processId: {}", activity.type(), activity.definitionId(), activity.processId());
        var activityToRun = processInputVariables(activity);
        behaviorResolver.resolveBehavior(activity.type()).run(activityToRun);
        log.debug("Run {} activity with definitionId: {} and processId: {} success", activity.type(), activity.definitionId(), activity.processId());
    }

    @Override
    public Class<RunActivityCommand> getCommandType() {
        return RunActivityCommand.class;
    }

    private ActivityExecution processInputVariables(ActivityExecution activity) {
        var variables = variablesService.evaluate(activity, activity.inputs());
        return activity.toBuilder().variables(variables).build();
    }

    private ActivityExecution getActivity(RunActivityCommand command) {
        if (command.activity() != null) {
            return command.activity();
        }

        if (command.definitionId() != null) {
            return activityFactory.getNewByDefinitionId(command.definitionId(), command.processId());
        }

        return activityFactory.createActivity(command.definition(), command.process());
    }

    private boolean canHandle(ActivityExecution activity) {
        return !activity.process().isInTerminalState() || activity.isAsync();
    }

}
