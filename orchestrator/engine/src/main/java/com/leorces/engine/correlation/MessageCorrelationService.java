package com.leorces.engine.correlation;

import com.leorces.engine.event.EngineEventBus;
import com.leorces.engine.event.activity.ActivityEvent;
import com.leorces.engine.event.correlation.CorrelateMessageEvent;
import com.leorces.engine.exception.correlation.MessageCorrelationException;
import com.leorces.engine.variables.VariableRuntimeService;
import com.leorces.model.definition.activity.MessageActivityDefinition;
import com.leorces.model.runtime.process.Process;
import com.leorces.persistence.ProcessPersistence;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class MessageCorrelationService {

    private final ProcessPersistence processPersistence;
    private final VariableRuntimeService variableRuntimeService;
    private final EngineEventBus eventBus;

    @Async
    @EventListener
    void handleMessage(CorrelateMessageEvent event) {
        correlate(event.messageName, event.businessKey, event.correlationKeys, event.processVariables);
    }

    private void correlate(String messageName,
                           String businessKey,
                           Map<String, Object> correlationKeys,
                           Map<String, Object> processVariables) {
        var correlatedProcesses = findCorrelatedProcesses(messageName, businessKey, correlationKeys);
        validateCorrelatedProcesses(messageName, correlatedProcesses);

        var correlatedProcess = correlatedProcesses.getFirst();
        var correlatedActivities = correlateActivities(messageName, correlatedProcess);

        setVariables(correlatedProcess, processVariables);
        triggerActivities(correlatedProcess, correlatedActivities);
    }

    private List<Process> findCorrelatedProcesses(String messageName,
                                                  String businessKey,
                                                  Map<String, Object> correlationKeys) {
        return findProcesses(businessKey, correlationKeys).stream()
                .filter(process -> isCorrelated(messageName, process))
                .toList();
    }

    private List<Process> findProcesses(String businessKey, Map<String, Object> correlationKeys) {
        if (StringUtils.isNotBlank(businessKey) && !correlationKeys.isEmpty()) {
            return processPersistence.findByBusinessKeyAndVariables(businessKey, correlationKeys);
        }
        if (StringUtils.isNotBlank(businessKey)) {
            return processPersistence.findByBusinessKey(businessKey);
        }
        if (!correlationKeys.isEmpty()) {
            return processPersistence.findByVariables(correlationKeys);
        }

        throw MessageCorrelationException.missingBusinessKeyAndCorrelationKeys();
    }

    private boolean isCorrelated(String messageName, Process process) {
        return process.definition().messages().stream()
                .anyMatch(messageName::equals);
    }

    private void validateCorrelatedProcesses(String messageName, List<Process> processes) {
        if (processes.isEmpty()) {
            log.warn("No process correlated with message: {}", messageName);
            throw MessageCorrelationException.noProcessesCorrelated(messageName);
        }

        if (processes.size() > 1) {
            log.warn("Found more than one process correlated with message: {}", messageName);
            throw MessageCorrelationException.multipleProcessesCorrelated(messageName);
        }

        var process = processes.getFirst();
        if (process.state().isTerminal()) {
            log.warn("Process: {} is not in terminal state", process.id());
            throw MessageCorrelationException.invalidProcessState(process.id());
        }
    }

    private List<MessageActivityDefinition> correlateActivities(String messageName, Process process) {
        return process.definition().activities().stream()
                .filter(MessageActivityDefinition.class::isInstance)
                .map(MessageActivityDefinition.class::cast)
                .filter(definition -> messageName.equals(definition.messageReference()))
                .toList();
    }

    private void setVariables(Process process, Map<String, Object> processVariables) {
        if (!processVariables.isEmpty()) {
            variableRuntimeService.setProcessVariables(process, processVariables);
        }
    }

    private void triggerActivities(Process process, List<MessageActivityDefinition> activities) {
        activities.stream()
                .map(definition -> ActivityEvent.triggerByDefinitionAsync(definition, process))
                .forEach(eventBus::publish);
    }

}
