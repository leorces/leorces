package com.leorces.engine.activity.handler;

import com.leorces.engine.activity.behaviour.ActivityBehaviorResolver;
import com.leorces.engine.activity.command.CancelAllActivitiesCommand;
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
public class CancelAllActivitiesCommandHandler implements CommandHandler<CancelAllActivitiesCommand> {

    private final ActivityBehaviorResolver behaviorResolver;
    private final TaskExecutorService taskExecutor;

    @Override
    public void handle(CancelAllActivitiesCommand command) {
        var activities = command.activities();
        var activityIds = activities.stream().map(ActivityExecution::id).toList();

        log.debug("Cancel all activities: {}", activityIds);
        CompletableFuture.allOf(
                activities.stream()
                        .map(this::cancelAsync)
                        .toArray(CompletableFuture[]::new)
        ).join();
        log.debug("All activities: {} canceled", activityIds);
    }

    @Override
    public Class<CancelAllActivitiesCommand> getCommandType() {
        return CancelAllActivitiesCommand.class;
    }

    private CompletableFuture<Void> cancelAsync(ActivityExecution activity) {
        return taskExecutor.submit(() -> behaviorResolver.resolveBehavior(activity.type()).cancel(activity));
    }

}
