package com.leorces.engine.correlation.handler;

import com.leorces.api.exception.ExecutionException;
import com.leorces.engine.activity.command.TriggerActivityCommand;
import com.leorces.engine.core.CommandDispatcher;
import com.leorces.engine.core.CommandHandler;
import com.leorces.engine.correlation.command.CorrelateMessageCommand;
import com.leorces.engine.variables.command.SetVariablesCommand;
import com.leorces.model.definition.activity.MessageActivityDefinition;
import com.leorces.model.runtime.process.Process;
import com.leorces.model.search.ProcessFilter;
import com.leorces.persistence.ProcessPersistence;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class CorrelateMessageCommandHandler implements CommandHandler<CorrelateMessageCommand> {

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

    private List<Process> findCorrelatedProcesses(String messageName,
                                                  String businessKey,
                                                  Map<String, Object> correlationKeys) {
        return findProcesses(businessKey, correlationKeys).stream()
                .filter(process -> isCorrelated(messageName, process))
                .toList();
    }

    private List<Process> findProcesses(String businessKey, Map<String, Object> correlationKeys) {
        return processPersistence.findAll(
                ProcessFilter.builder()
                        .businessKey(businessKey)
                        .variables(correlationKeys)
                        .build()
        );
    }

    private boolean isCorrelated(String messageName, Process process) {
        return process.definition().messages().stream()
                .anyMatch(messageName::equals);
    }

    private void validateCorrelatedProcesses(String messageName, List<Process> processes) {
        if (processes.isEmpty()) {
            log.warn("No process correlated with message: {}", messageName);
            throw ExecutionException.of("Message correlation error", "No process correlated with message: %s".formatted(messageName));
        }

        if (processes.size() > 1) {
            log.warn("Found more than one process correlated with message: {}", messageName);
            throw ExecutionException.of("Message correlation error", "Found more than one process correlated with message: %s".formatted(messageName));
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
            dispatcher.dispatch(SetVariablesCommand.of(process, processVariables));
        }
    }

    private void triggerActivities(Process process, List<MessageActivityDefinition> activities) {
        activities.stream()
                .map(definition -> TriggerActivityCommand.of(process, definition))
                .forEach(dispatcher::dispatchAsync);
    }

}
