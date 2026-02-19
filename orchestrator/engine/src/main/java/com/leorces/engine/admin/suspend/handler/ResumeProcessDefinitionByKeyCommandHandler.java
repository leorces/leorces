package com.leorces.engine.admin.suspend.handler;

import com.leorces.engine.admin.common.handler.AbstractJobHandler;
import com.leorces.engine.admin.common.model.JobType;
import com.leorces.engine.admin.suspend.command.ResumeProcessDefinitionByKeyCommand;
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
public class ResumeProcessDefinitionByKeyCommandHandler extends AbstractJobHandler<ResumeProcessDefinitionByKeyCommand> {

    private static final String OUTPUT_TOTAL_RESUMED_PROCESSES = "Total resumed processes";

    private final AdminPersistence adminPersistence;
    private final ProcessPersistence processPersistence;
    private final DefinitionPersistence definitionPersistence;
    private final TaskExecutorService taskExecutor;
    private final SuspendProcessDefinitionProperties properties;

    public ResumeProcessDefinitionByKeyCommandHandler(JobPersistence jobPersistence,
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
    protected Map<String, Object> execute(Job job, ResumeProcessDefinitionByKeyCommand command) {
        var processDefinitionKey = command.processDefinitionKey();
        definitionPersistence.resumeByKey(processDefinitionKey);
        var resumedProcesses = resumeProcesses(processDefinitionKey);
        return Map.of(OUTPUT_TOTAL_RESUMED_PROCESSES, resumedProcesses);
    }

    @Override
    protected JobType getJobType() {
        return JobType.PROCESS_RESUME;
    }

    @Override
    public Class<ResumeProcessDefinitionByKeyCommand> getCommandType() {
        return ResumeProcessDefinitionByKeyCommand.class;
    }

    private long resumeProcesses(String processDefinitionKey) {
        var futures = IntStream.range(0, properties.maxJobs())
                .mapToObj(i -> resumeBatchAsync(processDefinitionKey))
                .toList();

        return futures.stream()
                .mapToLong(CompletableFuture::join)
                .sum();
    }

    private CompletableFuture<Long> resumeBatchAsync(String processDefinitionKey) {
        return taskExecutor.supplyAsync(() -> {
            long totalCompacted = 0;
            int batchSize;
            do {
                batchSize = resumeBatch(processDefinitionKey);
                totalCompacted += batchSize;
                log.info("Resumed {} processes in this batch for definitionKey: {}", batchSize, processDefinitionKey);
            } while (batchSize >= properties.batchSize());
            return totalCompacted;
        });
    }

    private int resumeBatch(String processDefinitionKey) {
        return adminPersistence.execute(
                () -> processPersistence.resumeByDefinitionKey(processDefinitionKey, properties.batchSize()));
    }

}
