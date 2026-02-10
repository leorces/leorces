package com.leorces.engine.process.handler;

import com.leorces.api.exception.ExecutionException;
import com.leorces.common.mapper.VariablesMapper;
import com.leorces.engine.core.CommandDispatcher;
import com.leorces.engine.core.ResultCommandHandler;
import com.leorces.engine.process.command.CreateProcessByCallActivityCommand;
import com.leorces.engine.process.command.CreateProcessCommand;
import com.leorces.model.definition.ProcessDefinition;
import com.leorces.model.runtime.process.Process;
import com.leorces.persistence.DefinitionPersistence;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class CreateProcessCommandHandler implements ResultCommandHandler<CreateProcessCommand, Process> {

    private final VariablesMapper variablesMapper;
    private final DefinitionPersistence definitionPersistence;
    private final CommandDispatcher dispatcher;

    @Override
    public Process execute(CreateProcessCommand command) {
        var callActivity = command.callActivity();
        var definitionId = command.definitionId();
        var definitionKey = command.definitionKey();

        if (callActivity != null) {
            return dispatcher.execute(CreateProcessByCallActivityCommand.of(callActivity));
        }

        ProcessDefinition definition = null;

        if (definitionId != null) {
            definition = findDefinitionById(command.definitionId());
        } else if (definitionKey != null) {
            definition = findLatestDefinition(command.definitionKey());
        }

        if (definition == null) {
            throw ExecutionException.of("Can't find definition for key: %s or id: %s".formatted(definitionKey, definitionId));
        }

        return buildProcess(definition, command.businessKey(), command.variables());
    }

    @Override
    public Class<CreateProcessCommand> getCommandType() {
        return CreateProcessCommand.class;
    }

    private ProcessDefinition findDefinitionById(String definitionId) {
        return definitionPersistence.findById(definitionId)
                .orElseThrow(() -> ExecutionException.of("Process definition not found", "Process definition not found by id: %s".formatted(definitionId)));
    }

    private ProcessDefinition findLatestDefinition(String definitionKey) {
        return definitionPersistence.findLatestByKey(definitionKey)
                .orElseThrow(() -> ExecutionException.of("Process definition not found", "Latest process definition not found for key: %s".formatted(definitionKey)));
    }

    private Process buildProcess(ProcessDefinition definition, String businessKey, Map<String, Object> variables) {
        return Process.builder()
                .businessKey(businessKey)
                .variables(variablesMapper.map(variables))
                .definition(definition)
                .build();
    }

}
