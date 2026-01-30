package com.leorces.engine.activity.handler;

import com.leorces.api.exception.ExecutionException;
import com.leorces.engine.activity.command.FindActivityCommand;
import com.leorces.engine.core.ResultCommandHandler;
import com.leorces.model.runtime.activity.ActivityExecution;
import com.leorces.persistence.ActivityPersistence;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FindActivityCommandHandler implements ResultCommandHandler<FindActivityCommand, ActivityExecution> {

    private final ActivityPersistence activityPersistence;

    @Override
    public ActivityExecution execute(FindActivityCommand command) {
        return command.activityId() != null
                ? findById(command.activityId())
                : findByDefinitionId(command.processId(), command.definitionId());
    }

    @Override
    public Class<FindActivityCommand> getCommandType() {
        return FindActivityCommand.class;
    }

    private ActivityExecution findById(String activityId) {
        return activityPersistence.findById(activityId)
                .orElseThrow(() -> ExecutionException.of("Activity not found", "Activity with id: %s not found".formatted(activityId)));
    }

    private ActivityExecution findByDefinitionId(String processId, String definitionId) {
        return activityPersistence.findByDefinitionId(processId, definitionId)
                .orElseThrow(() -> ExecutionException.of("Activity not found", "Activity not found for process: %s and definition: %s".formatted(processId, definitionId)));
    }

}
