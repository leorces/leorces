package com.leorces.engine.job.compaction.handler;

import com.leorces.engine.configuration.properties.job.CompactionProperties;
import com.leorces.engine.job.common.handler.AbstractJobHandler;
import com.leorces.engine.job.common.model.JobType;
import com.leorces.engine.job.compaction.command.CompactionCommand;
import com.leorces.engine.service.TaskExecutorService;
import com.leorces.model.job.Job;
import com.leorces.persistence.AdminPersistence;
import com.leorces.persistence.HistoryPersistence;
import com.leorces.persistence.JobPersistence;
import com.leorces.persistence.ProcessPersistence;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.IntStream;

@Slf4j
@Component
public class CompactionCommandHandler extends AbstractJobHandler<CompactionCommand> {

    private static final String OUTPUT_TOTAL_COMPACTED_PROCESSES = "Total compacted processes";

    private final ProcessPersistence processPersistence;
    private final HistoryPersistence historyPersistence;
    private final AdminPersistence adminPersistence;
    private final TaskExecutorService taskExecutor;
    private final CompactionProperties properties;

    public CompactionCommandHandler(JobPersistence jobPersistence,
                                    ProcessPersistence processPersistence,
                                    HistoryPersistence historyPersistence,
                                    AdminPersistence adminPersistence,
                                    TaskExecutorService taskExecutor,
                                    CompactionProperties properties) {
        super(jobPersistence);
        this.processPersistence = processPersistence;
        this.historyPersistence = historyPersistence;
        this.adminPersistence = adminPersistence;
        this.taskExecutor = taskExecutor;
        this.properties = properties;
    }

    @Override
    protected Map<String, Object> execute(Job job, CompactionCommand command) {
        long totalCompacted = compactProcesses();
        return Map.of(OUTPUT_TOTAL_COMPACTED_PROCESSES, totalCompacted);
    }

    @Override
    protected JobType getJobType() {
        return JobType.COMPACTION;
    }

    @Override
    public Class<CompactionCommand> getCommandType() {
        return CompactionCommand.class;
    }

    private long compactProcesses() {
        var futures = IntStream.range(0, properties.maxJobs())
                .mapToObj(i -> compactBatchAsync())
                .toList();

        return futures.stream()
                .mapToLong(CompletableFuture::join)
                .sum();
    }

    private CompletableFuture<Long> compactBatchAsync() {
        return taskExecutor.supplyAsync(() -> {
            long totalCompacted = 0;
            int batchSize;
            do {
                batchSize = compactBatch(properties.batchSize());
                totalCompacted += batchSize;
                log.info("Compacted {} completed processes in this batch", batchSize);
            } while (batchSize >= properties.batchSize());
            return totalCompacted;
        });
    }

    private int compactBatch(int batchSize) {
        return adminPersistence.execute(() -> {
            var completedProcesses = processPersistence.findAllFullyCompletedForUpdate(batchSize);
            if (completedProcesses.isEmpty()) {
                log.debug("No completed processes found for compaction");
                return 0;
            }

            historyPersistence.save(completedProcesses);
            return completedProcesses.size();
        });
    }

}

