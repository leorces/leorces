package com.leorces.engine.job.compaction;

import com.leorces.engine.configuration.properties.job.CompactionProperties;
import com.leorces.engine.core.CommandDispatcher;
import com.leorces.engine.job.compaction.command.CompactionCommand;
import com.leorces.engine.scheduler.ShedlockService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Slf4j
@Component
@RequiredArgsConstructor
public class CompactionScheduler implements SchedulingConfigurer {

    private static final String COMPACTION_JOB = "compaction-job";

    private final ShedlockService shedlockService;
    private final CommandDispatcher dispatcher;
    private final CompactionProperties compactionProperties;

    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        if (!compactionProperties.enabled()) {
            log.info("Compaction is disabled, scheduler not registered.");
            return;
        }

        log.info("Registering compaction scheduler with cron: {}", compactionProperties.cron());
        taskRegistrar.addCronTask(this::doCompaction, compactionProperties.cron());
    }

    private void doCompaction() {
        log.info("Scheduled compaction started");
        shedlockService.executeWithLock(COMPACTION_JOB, Duration.ofMinutes(60), () -> {
            dispatcher.dispatch(CompactionCommand.cron());
            return null;
        });
    }

}
