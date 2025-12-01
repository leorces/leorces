package com.leorces.engine.process.handler;

import com.leorces.engine.activity.command.CompleteActivityCommand;
import com.leorces.engine.core.CommandDispatcher;
import com.leorces.engine.core.CommandHandler;
import com.leorces.engine.exception.process.ProcessNotFoundException;
import com.leorces.engine.process.command.CompleteProcessCommand;
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
public class CompleteProcessCommandHandler implements CommandHandler<CompleteProcessCommand> {

    private final ProcessPersistence processPersistence;
    private final ActivityPersistence activityPersistence;
    private final ProcessMetrics processMetrics;
    private final CommandDispatcher dispatcher;

    @Override
    public void handle(CompleteProcessCommand command) {
        var process = getProcess(command);
        var processId = process.id();

        if (!process.isActive() || !isAllActivitiesCompleted(processId)) {
            log.debug("Can't complete process: {}", processId);
            return;
        }

        log.debug("Complete process: {}", processId);
        completeProcess(process);
    }

    @Override
    public Class<CompleteProcessCommand> getCommandType() {
        return CompleteProcessCommand.class;
    }

    private void completeProcess(Process process) {
        processPersistence.complete(process.id());
        processMetrics.recordProcessCompletedMetric(process);
        if (process.isCallActivity()) {
            dispatcher.dispatchAsync(CompleteActivityCommand.of(process.id()));
        }
    }

    private boolean isAllActivitiesCompleted(String processId) {
        return activityPersistence.isAllCompleted(processId);
    }

    private Process getProcess(CompleteProcessCommand command) {
        return command.process() != null
                ? command.process()
                : processPersistence.findById(command.processId())
                .orElseThrow(ProcessNotFoundException::new);
    }

}
