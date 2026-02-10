package com.leorces.engine.process.handler;

import com.leorces.api.exception.ExecutionException;
import com.leorces.engine.activity.command.RunActivityCommand;
import com.leorces.engine.core.CommandDispatcher;
import com.leorces.engine.core.ResultCommandHandler;
import com.leorces.engine.process.command.CreateProcessCommand;
import com.leorces.engine.process.command.RecordProcessMetricCommand;
import com.leorces.engine.process.command.RunProcessCommand;
import com.leorces.model.runtime.process.Process;
import com.leorces.persistence.ProcessPersistence;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import static com.leorces.engine.constants.MetricConstants.PROCESS_STARTED;

@Slf4j
@Component
@RequiredArgsConstructor
public class RunProcessCommandHandler implements ResultCommandHandler<RunProcessCommand, Process> {

    private final ProcessPersistence processPersistence;
    private final CommandDispatcher dispatcher;

    @Override
    public Process execute(RunProcessCommand command) {
        var process = getProcess(command);

        log.debug("Run process with definitionId: {}, definitionKey: {} and definition version: {}", process.definitionId(), process.definitionKey(), process.definition().version());
        var newProcess = processPersistence.run(process);
        dispatcher.dispatchAsync(RecordProcessMetricCommand.of(PROCESS_STARTED, newProcess));
        startInitialActivity(newProcess);
        return newProcess;
    }

    @Override
    public void handle(RunProcessCommand command) {
        execute(command);
    }

    @Override
    public Class<RunProcessCommand> getCommandType() {
        return RunProcessCommand.class;
    }

    private void startInitialActivity(Process process) {
        var startActivity = process.definition().getStartActivity()
                .orElseThrow(() -> ExecutionException.of("Can't start process", "Start event not found in process: %s".formatted(process.id()), process));
        dispatcher.dispatch(RunActivityCommand.of(process, startActivity));
    }

    private Process getProcess(RunProcessCommand command) {
        if (command.process() != null) {
            return command.process();
        }
        return dispatcher.execute(CreateProcessCommand.of(
                command.callActivity(),
                command.definitionId(),
                command.definitionKey(),
                command.businessKey(),
                command.variables()
        ));
    }

}
