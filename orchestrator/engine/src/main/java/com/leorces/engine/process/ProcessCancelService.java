package com.leorces.engine.process;

import com.leorces.engine.event.EngineEventBus;
import com.leorces.engine.event.activity.ActivityEvent;
import com.leorces.engine.event.process.cancel.CancelProcessByIdEvent;
import com.leorces.engine.event.process.terminate.TerminateProcessByIdEvent;
import com.leorces.model.runtime.process.Process;
import com.leorces.persistence.ProcessPersistence;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
class ProcessCancelService {

    private final ProcessPersistence processPersistence;
    private final EngineEventBus eventBus;
    private final ProcessMetrics processMetrics;

    @EventListener
    void handleCancel(CancelProcessByIdEvent event) {
        processPersistence.findById(event.processId)
                .ifPresent(this::cancel);
    }

    @EventListener
    void handleTerminate(TerminateProcessByIdEvent event) {
        processPersistence.findById(event.processId)
                .ifPresent(this::terminate);
    }

    private void cancel(Process process) {
        eventBus.publish(ActivityEvent.cancelAllByProcessId(process.id()));
        processPersistence.cancel(process);
        processMetrics.recordProcessCancelledMetric(process);
    }

    private void terminate(Process process) {
        eventBus.publish(ActivityEvent.terminateAllByProcessId(process.id()));
        processPersistence.terminate(process);
        processMetrics.recordProcessTerminatedMetric(process);
    }

}
