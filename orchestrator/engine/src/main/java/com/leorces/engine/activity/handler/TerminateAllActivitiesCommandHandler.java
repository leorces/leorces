package com.leorces.engine.activity.handler;

import com.leorces.engine.activity.behaviour.ActivityBehaviorResolver;
import com.leorces.engine.activity.command.TerminateAllActivitiesCommand;
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
public class TerminateAllActivitiesCommandHandler implements CommandHandler<TerminateAllActivitiesCommand> {

    private final ActivityBehaviorResolver behaviorResolver;
    private final TaskExecutorService taskExecutor;

    @Override
    public void handle(TerminateAllActivitiesCommand command) {
        var activities = command.activities();
        var activityIds = activities.stream().map(ActivityExecution::id).toList();

        log.debug("Terminate all activities: {}", activityIds);
        CompletableFuture.allOf(
                activities.stream()
                        .map(this::terminateAsync)
                        .toArray(CompletableFuture[]::new)
        ).join();
    }

    @Override
    public Class<TerminateAllActivitiesCommand> getCommandType() {
        return TerminateAllActivitiesCommand.class;
    }

    private CompletableFuture<Void> terminateAsync(ActivityExecution activity) {
        return taskExecutor.submit(() -> behaviorResolver.resolveBehavior(activity.type()).terminate(activity, true));
    }

}
