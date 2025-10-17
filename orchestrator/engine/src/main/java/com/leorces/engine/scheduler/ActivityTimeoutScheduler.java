package com.leorces.engine.scheduler;

import com.leorces.engine.activity.command.FailActivitiesByTimeoutCommand;
import com.leorces.engine.core.CommandDispatcher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Slf4j
@Component
@RequiredArgsConstructor
public class ActivityTimeoutScheduler {

    private static final String ACTIVITY_TIMEOUT_JOB = "activity-timeout-job";

    private final CommandDispatcher dispatcher;
    private final ShedlockService shedlockService;

    @Scheduled(fixedRate = 10000) // 10000 milliseconds = 10 seconds
    public void execute() {
        shedlockService.executeWithLock(ACTIVITY_TIMEOUT_JOB, Duration.ofSeconds(30), () -> {
            dispatcher.dispatch(new FailActivitiesByTimeoutCommand());
            return null;
        });
    }

}
