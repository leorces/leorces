package com.leorces.engine.process.handler;

import com.leorces.engine.core.CommandHandler;
import com.leorces.engine.process.command.RunProcessCommand;
import com.leorces.engine.service.process.ProcessFactory;
import com.leorces.engine.service.process.ProcessRuntimeService;
import com.leorces.model.runtime.process.Process;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RunProcessCommandHandler implements CommandHandler<RunProcessCommand> {

    private final ProcessRuntimeService processRuntimeService;
    private final ProcessFactory processFactory;

    @Override
    public void handle(RunProcessCommand command) {
        var process = getProcess(command);

        log.debug("Run process with definitionId: {}, definitionKey: {} and definition version: {}", process.definitionId(), process.definitionKey(), process.definition().version());
        processRuntimeService.start(process);
        log.debug("Process {} started with definitionId: {}, definitionKey: {} and definition version: {}", process.id(), process.definitionId(), process.definitionKey(), process.definition().version());
    }

    @Override
    public Class<RunProcessCommand> getCommandType() {
        return RunProcessCommand.class;
    }

    private Process getProcess(RunProcessCommand command) {
        return processFactory.createByCallActivity(command.callActivity());
    }

}
