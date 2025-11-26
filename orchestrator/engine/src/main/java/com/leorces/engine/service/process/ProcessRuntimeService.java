package com.leorces.engine.service.process;

import com.leorces.engine.activity.command.RunActivityCommand;
import com.leorces.engine.core.CommandDispatcher;
import com.leorces.engine.exception.activity.ActivityNotFoundException;
import com.leorces.engine.exception.process.ProcessException;
import com.leorces.model.runtime.process.Process;
import com.leorces.model.search.ProcessFilter;
import com.leorces.persistence.ProcessPersistence;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service
@AllArgsConstructor
public class ProcessRuntimeService {

    private final ProcessFactory processFactory;
    private final ProcessPersistence processPersistence;
    private final ProcessMetrics processMetrics;
    private final CommandDispatcher dispatcher;

    public Process startByDefinitionId(String definitionId, String businessKey, Map<String, Object> variables) {
        var process = processFactory.createByDefinitionId(definitionId, businessKey, variables);
        return start(process);
    }

    public Process startByDefinitionKey(String key, String businessKey, Map<String, Object> variables) {
        var process = processFactory.createByDefinitionKey(key, businessKey, variables);
        return start(process);
    }

    public Process start(Process process) {
        var newProcess = processPersistence.run(process);
        processMetrics.recordProcessStartedMetric(process);
        startInitialActivity(newProcess);
        return newProcess;
    }

    public Process find(ProcessFilter filter) {
        var processes = processPersistence.findAll(filter);
        if (processes.size() > 1) {
            throw ProcessException.moreThenOneProcessesFound(filter);
        }
        return !processes.isEmpty() ? processes.getFirst() : null;
    }

    private void startInitialActivity(Process process) {
        var startActivity = process.definition().getStartActivity()
                .orElseThrow(() -> ActivityNotFoundException.startActivityNotFoundInProcess(process.id()));
        dispatcher.dispatch(RunActivityCommand.of(process, startActivity));
    }

}
