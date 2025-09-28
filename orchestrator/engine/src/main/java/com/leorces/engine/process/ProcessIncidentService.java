package com.leorces.engine.process;

import com.leorces.engine.event.EngineEventBus;
import com.leorces.engine.event.activity.ActivityEvent;
import com.leorces.engine.event.activity.complete.CompleteActivitySuccessEvent;
import com.leorces.engine.event.activity.fail.IncidentFailActivityEvent;
import com.leorces.engine.event.process.incident.IncidentProcessEventAsync;
import com.leorces.model.runtime.process.Process;
import com.leorces.model.runtime.process.ProcessState;
import com.leorces.persistence.ActivityPersistence;
import com.leorces.persistence.ProcessPersistence;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
class ProcessIncidentService {

    private final ProcessPersistence processPersistence;
    private final ActivityPersistence activityPersistence;
    private final EngineEventBus eventBus;
    private final ProcessMetrics processMetrics;

    @Async
    @EventListener
    void handleIncident(IncidentProcessEventAsync event) {
        incident(event.process);
    }

    @Async
    @EventListener
    void handleIncident(IncidentFailActivityEvent event) {
        incident(event.activity.process());
    }

    @Async
    @EventListener
    void handle(CompleteActivitySuccessEvent event) {
        var process = event.activity.process();
        if (process.state() == ProcessState.INCIDENT) {
            tryRecoverFromIncident(process);
        }
    }

    private void incident(Process process) {
        var incidentProcess = processPersistence.incident(process);
        processMetrics.recordProcessIncidentMetric(incidentProcess);
        if (incidentProcess.isCallActivity()) {
            eventBus.publish(ActivityEvent.failByIdAsync(incidentProcess.id(), Map.of()));
        }
    }

    private void tryRecoverFromIncident(Process process) {
        if (!activityPersistence.isAnyFailed(process.id())) {
            processPersistence.changeState(process.id(), ProcessState.ACTIVE);
            processMetrics.recordProcessRecoveredMetrics(process);
        }
    }

}
