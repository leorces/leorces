package com.leorces.engine.admin.migration.handler;

import com.leorces.engine.admin.common.handler.AbstractJobHandler;
import com.leorces.engine.admin.common.model.JobType;
import com.leorces.engine.admin.migration.command.ProcessMigrationWithInstructionsCommand;
import com.leorces.engine.admin.migration.command.SingleProcessMigrationCommand;
import com.leorces.engine.configuration.properties.job.ProcessMigrationProperties;
import com.leorces.engine.core.CommandDispatcher;
import com.leorces.engine.service.TaskExecutorService;
import com.leorces.model.definition.ProcessDefinition;
import com.leorces.model.job.Job;
import com.leorces.model.job.migration.ProcessMigrationPlan;
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
public class ProcessMigrationWithInstructionsCommandHandler
        extends AbstractJobHandler<ProcessMigrationWithInstructionsCommand>
        implements AbstractProcessMigrationCommandHandler {

    private static final String OUTPUT_TOTAL_MIGRATED_PROCESSES = "Total migrated processes";

    private final AdminPersistence adminPersistence;
    private final ProcessPersistence processPersistence;
    private final TaskExecutorService taskExecutor;
    private final CommandDispatcher dispatcher;
    private final ProcessMigrationProperties properties;

    public ProcessMigrationWithInstructionsCommandHandler(JobPersistence jobPersistence,
                                                          AdminPersistence adminPersistence,
                                                          ProcessPersistence processPersistence,
                                                          TaskExecutorService taskExecutor,
                                                          CommandDispatcher dispatcher,
                                                          ProcessMigrationProperties properties) {
        super(jobPersistence);
        this.adminPersistence = adminPersistence;
        this.processPersistence = processPersistence;
        this.taskExecutor = taskExecutor;
        this.dispatcher = dispatcher;
        this.properties = properties;
    }

    @Override
    protected Map<String, Object> execute(Job job, ProcessMigrationWithInstructionsCommand command) {
        var fromDefinition = command.fromDefinition();
        var toDefinition = command.toDefinition();
        var migration = command.migration();
        long totalMigrated = migrateProcesses(fromDefinition, toDefinition, migration);
        return Map.of(OUTPUT_TOTAL_MIGRATED_PROCESSES, totalMigrated);
    }

    @Override
    protected JobType getJobType() {
        return JobType.PROCESS_MIGRATION;
    }

    @Override
    public Class<ProcessMigrationWithInstructionsCommand> getCommandType() {
        return ProcessMigrationWithInstructionsCommand.class;
    }

    private long migrateProcesses(ProcessDefinition fromDefinition,
                                  ProcessDefinition toDefinition,
                                  ProcessMigrationPlan migration) {
        var futures = IntStream.range(0, properties.maxJobs())
                .mapToObj(i -> migrateJobBatchAsync(fromDefinition, toDefinition, migration))
                .toList();

        return futures.stream()
                .mapToLong(CompletableFuture::join)
                .sum();
    }

    private CompletableFuture<Long> migrateJobBatchAsync(ProcessDefinition fromDefinition,
                                                         ProcessDefinition toDefinition,
                                                         ProcessMigrationPlan migration) {
        return taskExecutor.supplyAsync(() -> {
            long migratedCount = 0;
            int batchSize;
            do {
                batchSize = migrateBatch(fromDefinition, toDefinition, migration);
                migratedCount += batchSize;
                log.info("Migrated {} processes in this batch", batchSize);
            } while (batchSize >= properties.batchSize());
            return migratedCount;
        });
    }

    private int migrateBatch(ProcessDefinition fromDefinition,
                             ProcessDefinition toDefinition,
                             ProcessMigrationPlan migration) {
        return adminPersistence.execute(() -> {
            var processes = processPersistence.findExecutionsForUpdate(
                    fromDefinition.id(),
                    properties.batchSize()
            );

            processes.forEach(process -> dispatcher.dispatch(
                    SingleProcessMigrationCommand.of(process, toDefinition, migration)
            ));

            return processes.size();
        });
    }

}
