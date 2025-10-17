package com.leorces.engine.process.handler;

import com.leorces.engine.activity.command.FailActivityCommand;
import com.leorces.engine.core.CommandDispatcher;
import com.leorces.engine.core.CommandHandler;
import com.leorces.engine.exception.process.ProcessNotFoundException;
import com.leorces.engine.process.ProcessMetrics;
import com.leorces.engine.process.command.IncidentProcessCommand;
import com.leorces.model.runtime.activity.ActivityFailure;
import com.leorces.model.runtime.process.Process;
import com.leorces.model.runtime.process.ProcessState;
import com.leorces.persistence.ProcessPersistence;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class IncidentProcessCommandHandler implements CommandHandler<IncidentProcessCommand> {

    private final ProcessPersistence processPersistence;
    private final ProcessMetrics processMetrics;
    private final CommandDispatcher dispatcher;

    @Override
    public void handle(IncidentProcessCommand command) {
        var process = getProcess(command);
        var processId = process.id();

        if (process.state() == ProcessState.INCIDENT) {
            log.debug("Process {} is already in INCIDENT state", processId);
            return;
        }

        incidentProcess(process);
    }

    @Override
    public Class<IncidentProcessCommand> getCommandType() {
        return IncidentProcessCommand.class;
    }

    private void incidentProcess(Process process) {
        processPersistence.incident(process);
        processMetrics.recordProcessIncidentMetric(process);
        if (process.isCallActivity()) {
            dispatcher.dispatchAsync(FailActivityCommand.of(process.id(), ActivityFailure.of("Process Incident")));
        }
    }

    private Process getProcess(IncidentProcessCommand command) {
        return processPersistence.findById(command.processId())
                .orElseThrow(ProcessNotFoundException::new);
    }

}