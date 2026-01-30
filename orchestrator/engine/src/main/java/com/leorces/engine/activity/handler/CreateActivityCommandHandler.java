package com.leorces.engine.activity.handler;

import com.leorces.api.exception.ExecutionException;
import com.leorces.engine.activity.command.CreateActivityCommand;
import com.leorces.engine.core.ResultCommandHandler;
import com.leorces.model.definition.activity.ActivityDefinition;
import com.leorces.model.runtime.activity.ActivityExecution;
import com.leorces.model.runtime.process.Process;
import com.leorces.persistence.ProcessPersistence;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CreateActivityCommandHandler implements ResultCommandHandler<CreateActivityCommand, ActivityExecution> {

    private final ProcessPersistence processPersistence;

    @Override
    public ActivityExecution execute(CreateActivityCommand command) {
        var process = getProcess(command);
        var definition = getDefinition(process, command);

        return createActivity(definition, process);
    }

    @Override
    public Class<CreateActivityCommand> getCommandType() {
        return CreateActivityCommand.class;
    }

    private ActivityExecution createActivity(ActivityDefinition definition, Process process) {
        return ActivityExecution.builder()
                .definitionId(definition.id())
                .process(process)
                .build();
    }

    private Process getProcess(CreateActivityCommand command) {
        return command.process() != null
                ? command.process()
                : processPersistence.findById(command.processId())
                .orElseThrow(() -> ExecutionException.of("Process not found", "Process with id: %s not found".formatted(command.processId())));
    }

    private ActivityDefinition getDefinition(Process process, CreateActivityCommand command) {
        return command.definition() != null
                ? command.definition()
                : process.definition().getActivityById(command.definitionId())
                .orElseThrow(() -> ExecutionException.of("Activity definition not found", "Activity definition not found for definitionId: %s in process: %s".formatted(command.definitionId(), process.id()), process));
    }

}
