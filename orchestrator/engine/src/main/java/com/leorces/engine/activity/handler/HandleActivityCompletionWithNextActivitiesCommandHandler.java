package com.leorces.engine.activity.handler;

import com.leorces.engine.activity.command.HandleActivityCompletionWithNextActivitiesCommand;
import com.leorces.engine.activity.command.RunActivityCommand;
import com.leorces.engine.core.CommandDispatcher;
import com.leorces.engine.core.CommandHandler;
import com.leorces.engine.process.command.ResolveProcessIncidentCommand;
import com.leorces.model.definition.activity.ActivityDefinition;
import com.leorces.model.runtime.process.Process;
import com.leorces.model.runtime.process.ProcessState;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class HandleActivityCompletionWithNextActivitiesCommandHandler
        implements CommandHandler<HandleActivityCompletionWithNextActivitiesCommand> {

    private final CommandDispatcher dispatcher;

    @Override
    public void handle(HandleActivityCompletionWithNextActivitiesCommand command) {
        var process = command.process();
        var nextActivities = command.nextActivities();

        resolveProcessIncidentIfNeeded(process);
        runNextActivities(nextActivities, process);
    }

    @Override
    public Class<HandleActivityCompletionWithNextActivitiesCommand> getCommandType() {
        return HandleActivityCompletionWithNextActivitiesCommand.class;
    }

    private void resolveProcessIncidentIfNeeded(Process process) {
        if (process.state() == ProcessState.INCIDENT) {
            dispatcher.dispatch(ResolveProcessIncidentCommand.of(process.id()));
        }
    }

    private void runNextActivities(List<ActivityDefinition> activities, Process process) {
        activities.stream()
                .map(nextActivity -> RunActivityCommand.of(process, nextActivity))
                .forEach(dispatcher::dispatchAsync);
    }

}
