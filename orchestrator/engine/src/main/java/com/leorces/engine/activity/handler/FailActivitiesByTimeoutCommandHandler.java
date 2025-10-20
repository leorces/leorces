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

    private static final int BATCH_SIZE = 100;

    private final ActivityPersistence activityPersistence;
    private final CommandDispatcher dispatcher;

    @Override
    public void handle(FailActivitiesByTimeoutCommand command) {
        int timedOutCount;

        do {
            log.debug("Starting failure of timed out activities");
            var activities = activityPersistence.findTimedOut(BATCH_SIZE);

            if (activities.isEmpty()) {
                log.debug("No timed out activities found");
                return;
            }

            activities.forEach(this::failActivity);
            timedOutCount = activities.size();
        } while (timedOutCount >= BATCH_SIZE);
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
