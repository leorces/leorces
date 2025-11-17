package com.leorces.engine.correlation.handler;

import com.leorces.engine.activity.command.TriggerActivityCommand;
import com.leorces.engine.core.CommandDispatcher;
import com.leorces.engine.core.CommandHandler;
import com.leorces.engine.correlation.command.CorrelateMessageCommand;
import com.leorces.engine.exception.correlation.MessageCorrelationException;
import com.leorces.engine.service.variable.VariablesService;
import com.leorces.engine.variables.command.SetVariablesCommand;
import com.leorces.model.definition.activity.MessageActivityDefinition;
import com.leorces.model.runtime.process.Process;
import com.leorces.persistence.ProcessPersistence;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class CorrelateMessageCommandHandler implements CommandHandler<CorrelateMessageCommand> {

    private final VariablesService variablesService;
    private final ProcessPersistence processPersistence;
    private final CommandDispatcher dispatcher;

    @Override
    public void handle(CorrelateMessageCommand command) {
        var messageName = command.messageName();
        var businessKey = command.businessKey();
        var correlationKeys = command.correlationKeys();
        var processVariables = command.processVariables();

        var correlatedProcesses = findCorrelatedProcesses(messageName, businessKey, correlationKeys);
        validateCorrelatedProcesses(messageName, correlatedProcesses);

        var correlatedProcess = correlatedProcesses.getFirst();
        var correlatedActivities = correlateActivities(messageName, correlatedProcess);

        setVariables(correlatedProcess, processVariables);
        triggerActivities(correlatedProcess, correlatedActivities);
    }

    @Override
    public Class<CorrelateMessageCommand> getCommandType() {
        return CorrelateMessageCommand.class;
    }

    private List<com.leorces.model.runtime.process.Process> findCorrelatedProcesses(String messageName,
                                                                                    String businessKey,
                                                                                    Map<String, Object> correlationKeys) {
        return findProcesses(businessKey, correlationKeys).stream()
                .filter(process -> isCorrelated(messageName, process))
                .toList();
    }

    private List<com.leorces.model.runtime.process.Process> findProcesses(String businessKey, Map<String, Object> correlationKeys) {
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

    private boolean isCorrelated(String messageName, com.leorces.model.runtime.process.Process process) {
        return process.definition().messages().stream()
                .anyMatch(messageName::equals);
    }

    private void validateCorrelatedProcesses(String messageName, List<com.leorces.model.runtime.process.Process> processes) {
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

    private List<MessageActivityDefinition> correlateActivities(String messageName, com.leorces.model.runtime.process.Process process) {
        return process.definition().activities().stream()
                .filter(MessageActivityDefinition.class::isInstance)
                .map(MessageActivityDefinition.class::cast)
                .filter(definition -> messageName.equals(definition.messageReference()))
                .toList();
    }

    private void setVariables(Process process, Map<String, Object> processVariables) {
        if (!processVariables.isEmpty()) {
            dispatcher.dispatch(SetVariablesCommand.of(process, processVariables));
        }
    }

    private void triggerActivities(Process process, List<MessageActivityDefinition> activities) {
        activities.stream()
                .map(definition -> TriggerActivityCommand.of(process, definition))
                .forEach(dispatcher::dispatchAsync);
    }

}
