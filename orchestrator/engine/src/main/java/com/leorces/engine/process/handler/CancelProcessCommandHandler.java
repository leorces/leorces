package com.leorces.engine.process.handler;

import com.leorces.engine.activity.command.CancelAllActivitiesCommand;
import com.leorces.engine.core.CommandDispatcher;
import com.leorces.engine.core.CommandHandler;
import com.leorces.engine.exception.process.ProcessNotFoundException;
import com.leorces.engine.process.ProcessMetrics;
import com.leorces.engine.process.command.CancelProcessCommand;
import com.leorces.model.runtime.process.Process;
import com.leorces.persistence.ActivityPersistence;
import com.leorces.persistence.ProcessPersistence;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CancelProcessCommandHandler implements CommandHandler<CancelProcessCommand> {

    private final ActivityPersistence activityPersistence;
    private final ProcessPersistence processPersistence;
    private final ProcessMetrics processMetrics;
    private final CommandDispatcher dispatcher;

    @Override
    public void handle(CancelProcessCommand command) {
        var process = getProcess(command);
        var processId = process.id();

        log.debug("Cancel process: {}", processId);
        var activeActivities = activityPersistence.findActive(processId);
        dispatcher.dispatch(CancelAllActivitiesCommand.of(activeActivities));
        processPersistence.cancel(process);
        processMetrics.recordProcessCancelledMetric(process);
        log.debug("Process {} canceled", processId);
    }

    @Override
    public Class<CancelProcessCommand> getCommandType() {
        return CancelProcessCommand.class;
    }

    private Process getProcess(CancelProcessCommand command) {
        return processPersistence.findById(command.processId())
                .orElseThrow(ProcessNotFoundException::new);
    }

}
