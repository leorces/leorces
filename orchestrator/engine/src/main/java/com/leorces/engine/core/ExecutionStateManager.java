package com.leorces.engine.core;

import com.leorces.model.runtime.process.ProcessState;
import com.leorces.persistence.ActivityPersistence;
import com.leorces.persistence.ProcessPersistence;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Manages synchronization of process and activity states.
 * Ensures state consistency between processes and their activities.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ExecutionStateManager {

    private final ProcessPersistence processPersistence;
    private final ActivityPersistence activityPersistence;

    public void syncProcessState(String processId) {
        var process = processPersistence.findById(processId);
        if (process.isEmpty()) {
            log.warn("Process not found: {}", processId);
            return;
        }

        var processExecution = process.get();
        var hasFailedActivities = activityPersistence.isAnyFailed(processId);
        var allActivitiesCompleted = activityPersistence.isAllCompleted(processId);

        if (hasFailedActivities && processExecution.state() != ProcessState.INCIDENT) {
            processPersistence.changeState(processId, ProcessState.INCIDENT);
            log.debug("Changed process {} state to INCIDENT due to failed activities", processId);
        } else if (!hasFailedActivities && processExecution.state() == ProcessState.INCIDENT) {
            processPersistence.changeState(processId, ProcessState.ACTIVE);
            log.debug("Recovered process {} state to ACTIVE", processId);
        } else if (allActivitiesCompleted && processExecution.state() == ProcessState.ACTIVE) {
            processPersistence.complete(processExecution);
            log.debug("Changed process {} state to COMPLETED", processId);
        }
    }

}