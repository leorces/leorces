package com.leorces.engine.activity.handler;

import com.leorces.engine.activity.command.RunActivityCommand;
import com.leorces.engine.activity.command.RunAllActivitiesCommand;
import com.leorces.engine.core.CommandDispatcher;
import com.leorces.engine.core.CommandHandler;
import com.leorces.engine.process.command.ResolveProcessIncidentCommand;
import com.leorces.model.definition.activity.ActivityDefinition;
import com.leorces.model.runtime.process.Process;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class RunAllActivitiesCommandHandler
        implements CommandHandler<RunAllActivitiesCommand> {

    private final CommandDispatcher dispatcher;

    @Override
    public void handle(RunAllActivitiesCommand command) {
        var process = command.process();
        var nextActivities = command.nextActivities();

        resolveProcessIncidentIfNeeded(process);
        runNextActivities(nextActivities, process);
    }

    @Override
    public Class<RunAllActivitiesCommand> getCommandType() {
        return RunAllActivitiesCommand.class;
    }

    private void resolveProcessIncidentIfNeeded(Process process) {
        if (process.isIncident()) {
            dispatcher.dispatch(ResolveProcessIncidentCommand.of(process.id()));
        }
    }

    private void runNextActivities(List<ActivityDefinition> activities, Process process) {
        activities.stream()
                .map(nextActivity -> RunActivityCommand.of(process, nextActivity))
                .forEach(dispatcher::dispatchAsync);
    }

}
