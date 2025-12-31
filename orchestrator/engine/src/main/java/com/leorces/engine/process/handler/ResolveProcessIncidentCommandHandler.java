package com.leorces.engine.process.handler;

import com.leorces.api.exception.ExecutionException;
import com.leorces.engine.core.CommandDispatcher;
import com.leorces.engine.core.CommandHandler;
import com.leorces.engine.process.command.ResolveProcessIncidentCommand;
import com.leorces.engine.service.process.ProcessMetrics;
import com.leorces.model.runtime.activity.ActivityExecution;
import com.leorces.model.runtime.activity.ActivityState;
import com.leorces.model.runtime.process.Process;
import com.leorces.model.runtime.process.ProcessState;
import com.leorces.persistence.ActivityPersistence;
import com.leorces.persistence.ProcessPersistence;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ResolveProcessIncidentCommandHandler implements CommandHandler<ResolveProcessIncidentCommand> {

    private final ProcessPersistence processPersistence;
    private final ActivityPersistence activityPersistence;
    private final CommandDispatcher commandDispatcher;
    private final ProcessMetrics processMetrics;

    @Override
    public void handle(ResolveProcessIncidentCommand command) {
        var process = getProcess(command);
        var processId = process.id();

        if (!process.isIncident()) {
            log.debug("Process: {} is not in INCIDENT state, current state: {}", processId, process.state());
            return;
        }

        if (!activityPersistence.isAnyFailed(processId)) {
            log.debug("Resolving incident in process: {}", processId);
            resolveProcessIncident(process);
        }
    }

    @Override
    public Class<ResolveProcessIncidentCommand> getCommandType() {
        return ResolveProcessIncidentCommand.class;
    }

    private void resolveProcessIncident(Process process) {
        processPersistence.changeState(process.id(), ProcessState.ACTIVE);
        processMetrics.recordProcessRecoveredMetrics(process);
        if (process.isCallActivity()) {
            var callActivity = getCallActivity(process.id());
            activityPersistence.changeState(callActivity.id(), ActivityState.ACTIVE);
            commandDispatcher.dispatch(ResolveProcessIncidentCommand.of(callActivity.processId()));
        }
    }

    private Process getProcess(ResolveProcessIncidentCommand command) {
        return processPersistence.findById(command.processId())
                .orElseThrow(() -> ExecutionException.of("Can't resolve incident in process", "Process with id: %s not found".formatted(command.processId())));
    }

    private ActivityExecution getCallActivity(String callActivityId) {
        return activityPersistence.findById(callActivityId)
                .orElseThrow(() -> ExecutionException.of("Can't resolve incident in call activity", "Call activity with id: %s not found".formatted(callActivityId)));
    }

}
