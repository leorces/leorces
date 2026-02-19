package com.leorces.engine.process.handler;

import com.leorces.engine.core.CommandHandler;
import com.leorces.engine.process.command.SuspendProcessCommand;
import com.leorces.persistence.ProcessPersistence;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SuspendProcessCommandHandler implements CommandHandler<SuspendProcessCommand> {

    private final ProcessPersistence processPersistence;

    @Override
    public void handle(SuspendProcessCommand command) {
        if (command.processId() != null) {
            processPersistence.suspendById(command.processId());
        }
    }

    @Override
    public Class<SuspendProcessCommand> getCommandType() {
        return SuspendProcessCommand.class;
    }

}
