package com.leorces.engine.admin.suspend.handler;

import com.leorces.api.exception.ExecutionException;
import com.leorces.engine.admin.common.handler.AbstractJobHandler;
import com.leorces.engine.admin.common.model.JobType;
import com.leorces.engine.admin.suspend.command.SuspendProcessDefinitionByIdCommand;
import com.leorces.engine.configuration.properties.job.SuspendProcessDefinitionProperties;
import com.leorces.engine.service.TaskExecutorService;
import com.leorces.model.definition.ProcessDefinition;
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
public class SuspendProcessDefinitionByIdCommandHandler extends AbstractJobHandler<SuspendProcessDefinitionByIdCommand> {

    private static final String OUTPUT_TOTAL_SUSPENDED_PROCESSES = "Total suspended processes";

    private final AdminPersistence adminPersistence;
    private final ProcessPersistence processPersistence;
    private final DefinitionPersistence definitionPersistence;
    private final TaskExecutorService taskExecutor;
    private final SuspendProcessDefinitionProperties properties;

    public SuspendProcessDefinitionByIdCommandHandler(JobPersistence jobPersistence,
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
    protected Map<String, Object> execute(Job job, SuspendProcessDefinitionByIdCommand command) {
        var key = command.processDefinitionKey();
        var version = command.processDefinitionVersion();
        var processDefinition = getProcessDefinition(key, version);

        definitionPersistence.suspendById(processDefinition.id());
        var suspendedProcesses = suspendProcesses(processDefinition.id());
        return Map.of(OUTPUT_TOTAL_SUSPENDED_PROCESSES, suspendedProcesses);
    }

    @Override
    protected JobType getJobType() {
        return JobType.PROCESS_SUSPEND;
    }

    @Override
    public Class<SuspendProcessDefinitionByIdCommand> getCommandType() {
        return SuspendProcessDefinitionByIdCommand.class;
    }

    private long suspendProcesses(String processDefinitionId) {
        var futures = IntStream.range(0, properties.maxJobs())
                .mapToObj(i -> suspendBatchAsync(processDefinitionId))
                .toList();

        return futures.stream()
                .mapToLong(CompletableFuture::join)
                .sum();
    }

    private CompletableFuture<Long> suspendBatchAsync(String processDefinitionId) {
        return taskExecutor.supplyAsync(() -> {
            long totalCompacted = 0;
            int batchSize;
            do {
                batchSize = suspendBatch(processDefinitionId);
                totalCompacted += batchSize;
                log.info("Suspended {} processes in this batch for definitionId: {}", batchSize, processDefinitionId);
            } while (batchSize >= properties.batchSize());
            return totalCompacted;
        });
    }

    private int suspendBatch(String processDefinitionId) {
        return adminPersistence.execute(
                () -> processPersistence.suspendByDefinitionId(processDefinitionId, properties.batchSize()));
    }

    private ProcessDefinition getProcessDefinition(String key, int version) {
        return definitionPersistence.findByKeyAndVersion(key, version)
                .orElseThrow(() -> ExecutionException.of("Process definition not found", "Can't suspend process definition with key: %s and version: %s".formatted(key, version)));
    }

}
