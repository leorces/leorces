package com.leorces.engine.process.handler;

import com.leorces.api.exception.ExecutionException;
import com.leorces.engine.activity.command.DeleteActivityCommand;
import com.leorces.engine.activity.command.DeleteAllActivitiesCommand;
import com.leorces.engine.core.CommandDispatcher;
import com.leorces.engine.core.CommandHandler;
import com.leorces.engine.process.command.DeleteProcessCommand;
import com.leorces.model.runtime.process.Process;
import com.leorces.persistence.ActivityPersistence;
import com.leorces.persistence.ProcessPersistence;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DeleteProcessCommandHandler implements CommandHandler<DeleteProcessCommand> {

    private final ActivityPersistence activityPersistence;
    private final ProcessPersistence processPersistence;
    private final CommandDispatcher dispatcher;

    @Override
    public void handle(DeleteProcessCommand command) {
        var process = getProcess(command);
        var processId = process.id();

        if (process.isInTerminalState()) {
            log.debug("Process {} is already in terminal state: {}", processId, process.state());
            return;
        }

        log.debug("Delete process: {}", processId);
        deleteActivities(processId);
        deleteProcess(process, command.deleteCallActivity());
    }

    @Override
    public Class<DeleteProcessCommand> getCommandType() {
        return DeleteProcessCommand.class;
    }

    private void deleteProcess(Process process, boolean deleteCallActivity) {
        processPersistence.delete(process.id());
        if (process.isCallActivity() && deleteCallActivity) {
            dispatcher.dispatch(DeleteActivityCommand.of(process.id()));
        }
    }

    private void deleteActivities(String processId) {
        var activeActivities = activityPersistence.findActive(processId);
        dispatcher.dispatch(DeleteAllActivitiesCommand.of(activeActivities));
    }

    private Process getProcess(DeleteProcessCommand command) {
        return processPersistence.findById(command.processId())
                .orElseThrow(() -> ExecutionException.of("Can't delete process", "Process with id: %s not found".formatted(command.processId())));
    }

}
