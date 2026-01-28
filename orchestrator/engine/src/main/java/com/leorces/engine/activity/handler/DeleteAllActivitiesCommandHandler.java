package com.leorces.engine.activity.handler;

import com.leorces.engine.activity.behaviour.ActivityBehaviorResolver;
import com.leorces.engine.activity.command.DeleteAllActivitiesCommand;
import com.leorces.engine.core.CommandHandler;
import com.leorces.engine.service.TaskExecutorService;
import com.leorces.model.runtime.activity.ActivityExecution;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
@RequiredArgsConstructor
public class DeleteAllActivitiesCommandHandler implements CommandHandler<DeleteAllActivitiesCommand> {

    private final ActivityBehaviorResolver behaviorResolver;
    private final TaskExecutorService taskExecutor;

    @Override
    public void handle(DeleteAllActivitiesCommand command) {
        var activities = command.activities();
        var activityIds = activities.stream().map(ActivityExecution::id).toList();

        log.debug("Delete all activities: {}", activityIds);
        CompletableFuture.allOf(
                activities.stream()
                        .map(this::deleteAsync)
                        .toArray(CompletableFuture[]::new)
        ).join();
    }

    @Override
    public Class<DeleteAllActivitiesCommand> getCommandType() {
        return DeleteAllActivitiesCommand.class;
    }

    private CompletableFuture<Void> deleteAsync(ActivityExecution activity) {
        return taskExecutor.runAsync(() -> behaviorResolver.resolveBehavior(activity.type()).delete(activity));
    }

}
