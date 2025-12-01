package com.leorces.engine.process.handler;

import com.leorces.engine.activity.command.TerminateActivityCommand;
import com.leorces.engine.activity.command.TerminateAllActivitiesCommand;
import com.leorces.engine.core.CommandDispatcher;
import com.leorces.engine.core.CommandHandler;
import com.leorces.engine.exception.process.ProcessNotFoundException;
import com.leorces.engine.process.command.TerminateProcessCommand;
import com.leorces.engine.service.process.ProcessMetrics;
import com.leorces.model.runtime.process.Process;
import com.leorces.persistence.ActivityPersistence;
import com.leorces.persistence.ProcessPersistence;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TerminateProcessCommandHandler implements CommandHandler<TerminateProcessCommand> {

    private final ActivityPersistence activityPersistence;
    private final ProcessPersistence processPersistence;
    private final ProcessMetrics processMetrics;
    private final CommandDispatcher dispatcher;

    @Override
    public void handle(TerminateProcessCommand command) {
        var process = getProcess(command);
        var processId = process.id();

        if (process.isInTerminalState()) {
            log.debug("Process {} is already completed, current state: {}", processId, process.state());
            return;
        }

        log.debug("Terminate process: {}", processId);
        terminateActivities(processId);
        terminateProcess(process, command.terminateCallActivity());
    }

    @Override
    public Class<TerminateProcessCommand> getCommandType() {
        return TerminateProcessCommand.class;
    }

    private void terminateProcess(Process process, boolean terminateCallActivity) {
        processPersistence.terminate(process.id());
        processMetrics.recordProcessTerminatedMetric(process);
        if (process.isCallActivity() && terminateCallActivity) {
            dispatcher.dispatch(TerminateActivityCommand.of(process.id()));
        }
    }

    private void terminateActivities(String processId) {
        var activeActivities = activityPersistence.findActive(processId);
        dispatcher.dispatch(TerminateAllActivitiesCommand.of(activeActivities));
    }

    private Process getProcess(TerminateProcessCommand command) {
        return processPersistence.findById(command.processId())
                .orElseThrow(ProcessNotFoundException::new);
    }

}
