package com.leorces.engine.process.handler;

import com.leorces.api.exception.ExecutionException;
import com.leorces.engine.core.CommandHandler;
import com.leorces.engine.process.command.ResumeProcessCommand;
import com.leorces.persistence.DefinitionPersistence;
import com.leorces.persistence.ProcessPersistence;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ResumeProcessCommandHandler implements CommandHandler<ResumeProcessCommand> {

    private final ProcessPersistence processPersistence;
    private final DefinitionPersistence definitionPersistence;

    @Override
    public void handle(ResumeProcessCommand command) {
        if (!command.isIdentifierPresent()) {
            log.warn("Can't resume process without identifier");
            throw ExecutionException.of("Can't resume process without identifier");
        }

        if (command.processId() != null) {
            processPersistence.resumeById(command.processId());
            return;
        }

        if (command.definitionId() != null) {
            definitionPersistence.resumeById(command.definitionId());
            processPersistence.resumeByDefinitionId(command.definitionId());
            return;
        }

        if (command.definitionKey() != null) {
            definitionPersistence.resumeByKey(command.definitionKey());
            processPersistence.resumeByDefinitionKey(command.definitionKey());
        }
    }

    @Override
    public Class<ResumeProcessCommand> getCommandType() {
        return ResumeProcessCommand.class;
    }

}
