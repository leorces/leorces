package com.leorces.engine.process.handler;

import com.leorces.api.exception.ExecutionException;
import com.leorces.engine.core.CommandDispatcher;
import com.leorces.engine.core.CommandHandler;
import com.leorces.engine.process.command.RecordProcessMetricCommand;
import com.leorces.engine.process.command.ResolveProcessIncidentCommand;
import com.leorces.model.runtime.activity.ActivityExecution;
import com.leorces.model.runtime.activity.ActivityState;
import com.leorces.model.runtime.process.Process;
import com.leorces.model.runtime.process.ProcessState;
import com.leorces.persistence.ActivityPersistence;
import com.leorces.persistence.ProcessPersistence;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import static com.leorces.engine.constants.MetricConstants.PROCESS_RECOVERED;

@Slf4j
@Component
@RequiredArgsConstructor
public class ResolveProcessIncidentCommandHandler implements CommandHandler<ResolveProcessIncidentCommand> {

    private final ProcessPersistence processPersistence;
    private final ActivityPersistence activityPersistence;
    private final CommandDispatcher dispatcher;

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
        dispatcher.dispatchAsync(RecordProcessMetricCommand.of(PROCESS_RECOVERED, process));
        if (process.isCallActivity()) {
            var callActivity = getCallActivity(process.id());
            activityPersistence.changeState(callActivity.id(), ActivityState.ACTIVE);
            dispatcher.dispatch(ResolveProcessIncidentCommand.of(callActivity.processId()));
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
