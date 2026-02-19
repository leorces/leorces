package com.leorces.engine.admin.suspend.handler;

import com.leorces.engine.admin.common.handler.AbstractJobHandler;
import com.leorces.engine.admin.common.model.JobType;
import com.leorces.engine.admin.suspend.command.SuspendProcessDefinitionByKeyCommand;
import com.leorces.engine.configuration.properties.job.SuspendProcessDefinitionProperties;
import com.leorces.engine.service.TaskExecutorService;
import com.leorces.model.job.Job;
import com.leorces.persistence.AdminPersistence;
import com.leorces.persistence.DefinitionPersistence;
import com.leorces.persistence.JobPersistence;
import com.leorces.persistence.ProcessPersistence;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.IntStream;

@Slf4j
@Component
public class SuspendProcessDefinitionByKeyCommandHandler extends AbstractJobHandler<SuspendProcessDefinitionByKeyCommand> {

    private static final String OUTPUT_TOTAL_SUSPENDED_PROCESSES = "Total suspended processes";

    private final AdminPersistence adminPersistence;
    private final ProcessPersistence processPersistence;
    private final DefinitionPersistence definitionPersistence;
    private final TaskExecutorService taskExecutor;
    private final SuspendProcessDefinitionProperties properties;

    public SuspendProcessDefinitionByKeyCommandHandler(JobPersistence jobPersistence,
                                                       AdminPersistence adminPersistence,
                                                       ProcessPersistence processPersistence,
                                                       DefinitionPersistence definitionPersistence,
                                                       TaskExecutorService taskExecutor,
                                                       SuspendProcessDefinitionProperties properties) {
        super(jobPersistence);
        this.adminPersistence = adminPersistence;
        this.processPersistence = processPersistence;
        this.definitionPersistence = definitionPersistence;
        this.taskExecutor = taskExecutor;
        this.properties = properties;
    }

    @Override
    protected Map<String, Object> execute(Job job, SuspendProcessDefinitionByKeyCommand command) {
        var processDefinitionKey = command.processDefinitionKey();
        definitionPersistence.suspendByKey(processDefinitionKey);
        var suspendedProcesses = suspendProcesses(processDefinitionKey);
        return Map.of(OUTPUT_TOTAL_SUSPENDED_PROCESSES, suspendedProcesses);
    }

    @Override
    protected JobType getJobType() {
        return JobType.PROCESS_SUSPEND;
    }

    @Override
    public Class<SuspendProcessDefinitionByKeyCommand> getCommandType() {
        return SuspendProcessDefinitionByKeyCommand.class;
    }

    private long suspendProcesses(String processDefinitionKey) {
        var futures = IntStream.range(0, properties.maxJobs())
                .mapToObj(i -> suspendBatchAsync(processDefinitionKey))
                .toList();

        return futures.stream()
                .mapToLong(CompletableFuture::join)
                .sum();
    }

    private CompletableFuture<Long> suspendBatchAsync(String processDefinitionKey) {
        return taskExecutor.supplyAsync(() -> {
            long totalCompacted = 0;
            int batchSize;
            do {
                batchSize = suspendBatch(processDefinitionKey);
                totalCompacted += batchSize;
                log.info("Suspended {} processes in this batch for definitionKey: {}", batchSize, processDefinitionKey);
            } while (batchSize >= properties.batchSize());
            return totalCompacted;
        });
    }

    private int suspendBatch(String processDefinitionKey) {
        return adminPersistence.execute(
                () -> processPersistence.suspendByDefinitionKey(processDefinitionKey, properties.batchSize()));
    }

}
