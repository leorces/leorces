package com.leorces.engine.job.migration.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.leorces.api.exception.ExecutionException;
import com.leorces.engine.core.CommandDispatcher;
import com.leorces.engine.core.CommandHandler;
import com.leorces.engine.job.migration.command.EasyProcessMigrationCommand;
import com.leorces.engine.job.migration.command.ProcessMigrationCommand;
import com.leorces.engine.job.migration.command.ProcessMigrationWithInstructionsCommand;
import com.leorces.engine.job.migration.command.ValidateProcessMigrationCommand;
import com.leorces.model.definition.ProcessDefinition;
import com.leorces.model.job.migration.ProcessMigrationPlan;
import com.leorces.persistence.DefinitionPersistence;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
public class ProcessMigrationCommandHandler implements
        AbstractProcessMigrationCommandHandler,
        CommandHandler<ProcessMigrationCommand> {

    private final DefinitionPersistence definitionPersistence;
    private final ObjectMapper objectMapper;
    private final CommandDispatcher dispatcher;

    public ProcessMigrationCommandHandler(DefinitionPersistence definitionPersistence,
                                          ObjectMapper objectMapper,
                                          CommandDispatcher dispatcher) {
        this.definitionPersistence = definitionPersistence;
        this.objectMapper = objectMapper;
        this.dispatcher = dispatcher;
    }

    @Override
    public void handle(ProcessMigrationCommand command) {
        var migration = getProcessMigration(command.input());
        var fromDefinition = getDefinition(migration.definitionKey(), migration.fromVersion());
        var toDefinition = getDefinition(migration.definitionKey(), migration.toVersion());

        dispatcher.dispatch(ValidateProcessMigrationCommand.of(fromDefinition, toDefinition, migration));

        if (isEasyMigration(fromDefinition, toDefinition, migration)) {
            dispatcher.dispatchAsync(
                    EasyProcessMigrationCommand.of(
                            fromDefinition,
                            toDefinition,
                            migration,
                            command.input()
                    )
            );
        } else {
            dispatcher.dispatchAsync(
                    ProcessMigrationWithInstructionsCommand.of(
                            fromDefinition,
                            toDefinition,
                            migration,
                            command.input()
                    )
            );
        }
    }

    @Override
    public Class<ProcessMigrationCommand> getCommandType() {
        return ProcessMigrationCommand.class;
    }

    private ProcessDefinition getDefinition(String definitionKey, Integer version) {
        return definitionPersistence.findByKeyAndVersion(definitionKey, version)
                .orElseThrow(() -> ExecutionException.of("Can't migrate process. %s process definition with version %s not found".formatted(definitionKey, version)));
    }

    private ProcessMigrationPlan getProcessMigration(Map<String, Object> input) {
        return objectMapper.convertValue(input, ProcessMigrationPlan.class);
    }

}
