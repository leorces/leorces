package com.leorces.engine.process;

import com.leorces.engine.event.EngineEventBus;
import com.leorces.engine.event.activity.ActivityEvent;
import com.leorces.engine.event.process.start.StartProcessByCallActivityEvent;
import com.leorces.engine.exception.activity.ActivityNotFoundException;
import com.leorces.model.runtime.process.Process;
import com.leorces.persistence.ProcessPersistence;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class ProcessStartService {

    private final ProcessFactory processFactory;
    private final ProcessPersistence processPersistence;
    private final EngineEventBus eventBus;
    private final ProcessMetrics processMetrics;

    @EventListener
    void handleStart(StartProcessByCallActivityEvent event) {
        start(processFactory.createByCallActivity(event.activity));
    }

    public Process startByDefinitionId(String definitionId, String businessKey, Map<String, Object> variables) {
        return start(processFactory.createByDefinitionId(definitionId, businessKey, variables));
    }

    public Process startByDefinitionKey(String key, String businessKey, Map<String, Object> variables) {
        return start(processFactory.createByDefinitionKey(key, businessKey, variables));
    }

    public Process start(Process process) {
        var newProcess = processPersistence.run(process);
        processMetrics.recordProcessStartedMetric(newProcess);
        startInitialActivity(newProcess);
        return newProcess;
    }

    private void startInitialActivity(Process process) {
        var startActivity = process.definition().getStartActivity()
                .orElseThrow(() -> ActivityNotFoundException.startActivityNotFoundInProcess(process.id()));
        eventBus.publish(ActivityEvent.runByDefinitionAsync(startActivity, process));
    }

}
