package com.leorces.engine.process.handler;

import com.leorces.api.exception.ExecutionException;
import com.leorces.engine.core.ResultCommandHandler;
import com.leorces.engine.process.command.FindProcessByFilterCommand;
import com.leorces.model.runtime.process.Process;
import com.leorces.persistence.ProcessPersistence;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class FindProcessByFilterCommandHandler implements ResultCommandHandler<FindProcessByFilterCommand, Process> {

    private final ProcessPersistence processPersistence;

    @Override
    public Process execute(FindProcessByFilterCommand command) {
        var filter = command.filter();
        var processes = processPersistence.findAll(filter);
        if (processes.size() > 1) {
            throw ExecutionException.of("More than one process found for filter: " + filter);
        }
        return !processes.isEmpty() ? processes.getFirst() : null;
    }

    @Override
    public Class<FindProcessByFilterCommand> getCommandType() {
        return FindProcessByFilterCommand.class;
    }

}
