package com.leorces.engine.activity.handler;

import com.leorces.engine.activity.behaviour.ActivityBehaviorResolver;
import com.leorces.engine.activity.command.CreateActivityCommand;
import com.leorces.engine.activity.command.RunActivityCommand;
import com.leorces.engine.core.CommandDispatcher;
import com.leorces.engine.core.CommandHandler;
import com.leorces.engine.process.command.ResolveProcessIncidentCommand;
import com.leorces.engine.variables.command.EvaluateVariablesCommand;
import com.leorces.model.runtime.activity.ActivityExecution;
import com.leorces.model.runtime.process.Process;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RunActivityCommandHandler implements CommandHandler<RunActivityCommand> {

    private final ActivityBehaviorResolver behaviorResolver;
    private final CommandDispatcher dispatcher;

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
        resolveProcessIncidentIfNeeded(activityToRun.process());
    }

    @Override
    public Class<RunActivityCommand> getCommandType() {
        return RunActivityCommand.class;
    }

    private ActivityExecution processInputVariables(ActivityExecution activity) {
        var variables = dispatcher.execute(EvaluateVariablesCommand.of(activity, activity.inputs()));
        return activity.toBuilder().variables(variables).build();
    }

    private void resolveProcessIncidentIfNeeded(Process process) {
        if (process.isIncident()) {
            dispatcher.dispatch(ResolveProcessIncidentCommand.of(process.id()));
        }
    }

    private ActivityExecution getActivity(RunActivityCommand command) {
        if (command.activity() != null) {
            return command.activity();
        }

        return dispatcher.execute(CreateActivityCommand.of(
                command.definition(),
                command.process(),
                command.definitionId(),
                command.processId()
        ));
    }

    private boolean canHandle(ActivityExecution activity) {
        return !activity.process().isInTerminalState() || activity.isAsync();
    }

}
