package com.leorces.engine.process.handler;

import com.leorces.api.exception.ExecutionException;
import com.leorces.engine.activity.command.FailActivityCommand;
import com.leorces.engine.core.CommandDispatcher;
import com.leorces.engine.core.CommandHandler;
import com.leorces.engine.process.command.IncidentProcessCommand;
import com.leorces.engine.process.command.RecordProcessMetricCommand;
import com.leorces.model.runtime.activity.ActivityFailure;
import com.leorces.model.runtime.process.Process;
import com.leorces.persistence.ProcessPersistence;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import static com.leorces.engine.constants.MetricConstants.PROCESS_INCIDENT;

@Slf4j
@Component
@RequiredArgsConstructor
public class IncidentProcessCommandHandler implements CommandHandler<IncidentProcessCommand> {

    private final ProcessPersistence processPersistence;
    private final CommandDispatcher dispatcher;

    @Override
    public void handle(IncidentProcessCommand command) {
        var process = getProcess(command);
        var processId = process.id();

        if (process.isIncident()) {
            log.debug("Process {} is already in INCIDENT state", processId);
            return;
        }

        log.debug("Incident process: {}", processId);
        incidentProcess(process);
    }

    @Override
    public Class<IncidentProcessCommand> getCommandType() {
        return IncidentProcessCommand.class;
    }

    private void incidentProcess(Process process) {
        processPersistence.incident(process.id());
        dispatcher.dispatchAsync(RecordProcessMetricCommand.of(PROCESS_INCIDENT, process));
        if (process.isCallActivity()) {
            dispatcher.dispatchAsync(FailActivityCommand.of(process.id(), ActivityFailure.of("Process Incident")));
        }
    }

    private Process getProcess(IncidentProcessCommand command) {
        return processPersistence.findById(command.processId())
                .orElseThrow(() -> ExecutionException.of("Can't incident process", "Process with id: %s not found".formatted(command.processId())));
    }

}