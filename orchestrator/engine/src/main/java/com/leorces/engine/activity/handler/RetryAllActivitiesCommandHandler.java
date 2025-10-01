package com.leorces.engine.activity.handler;

import com.leorces.engine.activity.behaviour.ActivityBehaviorResolver;
import com.leorces.engine.activity.command.RetryAllActivitiesCommand;
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
public class RetryAllActivitiesCommandHandler implements CommandHandler<RetryAllActivitiesCommand> {

    private final ActivityBehaviorResolver behaviorResolver;
    private final TaskExecutorService taskExecutor;

    @Override
    public void handle(RetryAllActivitiesCommand command) {
        var activities = command.activities();
        var activityIds = activities.stream().map(ActivityExecution::definitionId).toList();

        log.debug("Retry all activities: {}", activityIds);
        CompletableFuture.allOf(
                activities.stream()
                        .map(this::retryAsync)
                        .toArray(CompletableFuture[]::new)
        ).join();
        log.debug("All activities retried: {}", activityIds);
    }

    @Override
    public Class<RetryAllActivitiesCommand> getCommandType() {
        return RetryAllActivitiesCommand.class;
    }

    private CompletableFuture<Void> retryAsync(ActivityExecution activity) {
        return taskExecutor.submit(() -> behaviorResolver.resolveBehavior(activity.type()).retry(activity));
    }

}
