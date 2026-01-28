package com.leorces.engine.job.compaction.command;

import com.leorces.engine.job.common.command.JobCommand;

import java.util.Map;

public record CompactionCommand(
        Map<String, Object> input
) implements JobCommand {

    public static final String INPUT_TRIGGER_KEY = "Trigger";
    public static final String TRIGGER_MANUAL = "Triggered manually";
    public static final String TRIGGER_CRON = "Triggered by cron";

    public static CompactionCommand manual() {
        return new CompactionCommand(
                Map.of(INPUT_TRIGGER_KEY, TRIGGER_MANUAL)
        );
    }

    public static CompactionCommand cron() {
        return new CompactionCommand(
                Map.of(INPUT_TRIGGER_KEY, TRIGGER_CRON)
        );
    }

}
