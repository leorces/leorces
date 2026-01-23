package com.leorces.engine.job.compaction.command;

import com.leorces.engine.core.ExecutionCommand;

public record BatchCompactionCommand(
        int batchSize
) implements ExecutionCommand {

    public static BatchCompactionCommand of(int batchSize) {
        return new BatchCompactionCommand(batchSize);
    }

}
