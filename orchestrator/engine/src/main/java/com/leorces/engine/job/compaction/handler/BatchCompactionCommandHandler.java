package com.leorces.engine.job.compaction.handler;

import com.leorces.engine.core.CommandHandler;
import com.leorces.engine.job.compaction.command.BatchCompactionCommand;
import com.leorces.persistence.AdminPersistence;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class BatchCompactionCommandHandler implements CommandHandler<BatchCompactionCommand> {

    private final AdminPersistence adminPersistence;

    @Override
    public void handle(BatchCompactionCommand command) {
        int completedProcessesCount;
        int batchSize = command.batchSize();

        do {
            completedProcessesCount = adminPersistence.doCompaction(batchSize);
            log.info("Compacted {} completed processes", completedProcessesCount);
        } while (completedProcessesCount >= batchSize);
    }

    @Override
    public Class<BatchCompactionCommand> getCommandType() {
        return BatchCompactionCommand.class;
    }

}
