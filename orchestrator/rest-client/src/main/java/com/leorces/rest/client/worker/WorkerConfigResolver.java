package com.leorces.rest.client.worker;

import com.leorces.rest.client.configuration.properties.process.ProcessConfigurationProperties;
import com.leorces.rest.client.model.worker.WorkerMetadata;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class WorkerConfigResolver {

    private final ProcessConfigurationProperties processConfigurationProperties;

    public WorkerMetadata resolveWorkerConfig(TaskWorker annotation) {
        var processDefinitionKey = annotation.processDefinitionKey();
        var processConfig = processConfigurationProperties.configuration().get(processDefinitionKey);

        if (processConfig == null) {
            return new WorkerMetadata(
                    annotation.topic(),
                    annotation.processDefinitionKey(),
                    annotation.interval(),
                    annotation.initialDelay(),
                    annotation.maxConcurrentTasks(),
                    annotation.timeUnit()
            );
        }

        var workerConfig = processConfig.workers().get(annotation.topic());
        return new WorkerMetadata(
                annotation.topic(),
                annotation.processDefinitionKey(),
                workerConfig != null ? workerConfig.interval() : annotation.interval(),
                workerConfig != null ? workerConfig.initialDelay() : annotation.initialDelay(),
                workerConfig != null ? workerConfig.maxConcurrentTasks() : annotation.maxConcurrentTasks(),
                workerConfig != null ? workerConfig.timeUnit() : annotation.timeUnit()
        );
    }
}
