package com.leorces.engine.job.migration.handler;

import com.leorces.engine.configuration.properties.job.ProcessMigrationProperties;
import com.leorces.engine.job.common.handler.AbstractJobHandler;
import com.leorces.engine.job.common.model.JobType;
import com.leorces.engine.job.migration.command.EasyProcessMigrationCommand;
import com.leorces.engine.service.TaskExecutorService;
import com.leorces.model.job.Job;
import com.leorces.persistence.AdminPersistence;
import com.leorces.persistence.JobPersistence;
import com.leorces.persistence.ProcessPersistence;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.IntStream;

@Slf4j
@Component
public class EasyProcessMigrationCommandHandler
        extends AbstractJobHandler<EasyProcessMigrationCommand>
        implements AbstractProcessMigrationCommandHandler {

    private static final String OUTPUT_TOTAL_MIGRATED_PROCESSES = "Total migrated processes";

    private final AdminPersistence adminPersistence;
    private final ProcessPersistence processPersistence;
    private final TaskExecutorService taskExecutor;
    private final ProcessMigrationProperties properties;

    public EasyProcessMigrationCommandHandler(JobPersistence jobPersistence,
                                              AdminPersistence adminPersistence,
                                              ProcessPersistence processPersistence,
                                              TaskExecutorService taskExecutor,
                                              ProcessMigrationProperties properties) {
        super(jobPersistence);
        this.adminPersistence = adminPersistence;
        this.processPersistence = processPersistence;
        this.taskExecutor = taskExecutor;
        this.properties = properties;
    }

    @Override
    protected Map<String, Object> execute(Job job, EasyProcessMigrationCommand command) {
        var fromDefinitionId = command.fromDefinition().id();
        var toDefinitionId = command.toDefinition().id();
        long totalMigrated = migrateProcesses(fromDefinitionId, toDefinitionId);
        return Map.of(OUTPUT_TOTAL_MIGRATED_PROCESSES, totalMigrated);
    }

    @Override
    protected JobType getJobType() {
        return JobType.PROCESS_MIGRATION;
    }

    @Override
    public Class<EasyProcessMigrationCommand> getCommandType() {
        return EasyProcessMigrationCommand.class;
    }

    private long migrateProcesses(String fromDefinitionId,
                                  String toDefinitionId) {
        var futures = IntStream.range(0, properties.maxJobs())
                .mapToObj(i -> migrateJobBatchAsync(fromDefinitionId, toDefinitionId))
                .toList();

        return futures.stream()
                .mapToLong(CompletableFuture::join)
                .sum();
    }

    private CompletableFuture<Long> migrateJobBatchAsync(String fromDefinitionId,
                                                         String toDefinitionId) {
        return taskExecutor.supplyAsync(() -> {
            long migratedCount = 0;
            int batchSize;
            do {
                batchSize = migrateBatch(fromDefinitionId, toDefinitionId);
                migratedCount += batchSize;
                log.info("Migrated {} processes in this batch", batchSize);
            } while (batchSize >= properties.batchSize());
            return migratedCount;
        });
    }

    private int migrateBatch(String fromDefinitionId, String toDefinitionId) {
        return adminPersistence.execute(() ->
                processPersistence.updateDefinitionId(
                        fromDefinitionId,
                        toDefinitionId,
                        properties.batchSize()
                )
        );
    }

}
