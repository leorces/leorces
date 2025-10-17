package com.leorces.engine.activity.handler;

import com.leorces.engine.activity.command.FailActivitiesByTimeoutCommand;
import com.leorces.engine.activity.command.FailActivityCommand;
import com.leorces.engine.core.CommandDispatcher;
import com.leorces.engine.core.CommandHandler;
import com.leorces.model.runtime.activity.ActivityExecution;
import com.leorces.model.runtime.activity.ActivityFailure;
import com.leorces.persistence.ActivityPersistence;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class FailActivitiesByTimeoutCommandHandler implements CommandHandler<FailActivitiesByTimeoutCommand> {

    private final ActivityPersistence activityPersistence;
    private final CommandDispatcher dispatcher;

    @Override
    public void handle(FailActivitiesByTimeoutCommand command) {
        activityPersistence.findTimedOut()
                .forEach(this::failActivity);
    }

    @Override
    public Class<FailActivitiesByTimeoutCommand> getCommandType() {
        return FailActivitiesByTimeoutCommand.class;
    }

    private void failActivity(ActivityExecution activity) {
        log.debug("Activity {} timed out for process: {}", activity.id(), activity.processId());
        dispatcher.dispatchAsync(FailActivityCommand.of(activity, ActivityFailure.of("Timeout")));
    }

}
