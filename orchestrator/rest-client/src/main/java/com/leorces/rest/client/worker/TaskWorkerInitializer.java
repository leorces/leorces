package com.leorces.rest.client.worker;

import com.leorces.rest.client.exception.ClientConfigurationValidationException;
import com.leorces.rest.client.handler.ExternalTaskHandler;
import com.leorces.rest.client.model.worker.WorkerContext;
import lombok.AllArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@AllArgsConstructor
public class TaskWorkerInitializer implements ApplicationListener<ApplicationReadyEvent> {

    private final WorkerScheduler scheduler;
    private final WorkerConfigResolver workerConfigResolver;
    private final ApplicationContext applicationContext;

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        var taskHandlerBeans = applicationContext.getBeansOfType(ExternalTaskHandler.class);

        taskHandlerBeans.values().forEach(handler -> {
            var annotation = handler.getClass().getAnnotation(ExternalTaskSubscription.class);
            if (annotation != null) {
                getProcessDefinitionKeys(annotation).stream()
                        .filter(definitionKey -> !definitionKey.isBlank())
                        .forEach(processDefinitionKey -> {
                            var metadata = workerConfigResolver.resolveWorkerConfig(processDefinitionKey, annotation);
                            var context = WorkerContext.create(handler, metadata);
                            validate(context);
                            scheduler.startWorker(context);
                        });
            }
        });
    }

    private void validate(WorkerContext context) {
        var metadata = context.metadata();
        var handlerClassName = getHandlerClassName(context);

        if (metadata.topic().isBlank()) {
            throw new ClientConfigurationValidationException(
                    String.format("Topic cannot be blank for ExternalTaskHandler '%s'. Current value: '%s'. " +
                            "Please provide a non-empty topic name.", handlerClassName, metadata.topic())
            );
        }
        if (metadata.processDefinitionKey().isBlank()) {
            throw new ClientConfigurationValidationException(
                    String.format("ProcessDefinitionKey cannot be blank for ExternalTaskHandler '%s'. Current value: '%s'. " +
                            "Please provide a non-empty process definition key.", handlerClassName, metadata.processDefinitionKey())
            );
        }
        if (metadata.interval() <= 0) {
            throw new ClientConfigurationValidationException(
                    String.format("Interval must be positive for ExternalTaskHandler '%s'. Current value: %d %s. " +
                            "Please provide a value greater than 0.", handlerClassName, metadata.interval(), metadata.timeUnit())
            );
        }
        if (metadata.maxConcurrentTasks() <= 0) {
            throw new ClientConfigurationValidationException(
                    String.format("MaxConcurrentTasks must be positive for ExternalTaskHandler '%s'. Current value: %d. " +
                            "Please provide a value greater than 0.", handlerClassName, metadata.maxConcurrentTasks())
            );
        }
        if (metadata.initialDelay() < 0) {
            throw new ClientConfigurationValidationException(
                    String.format("InitialDelay must be non-negative for ExternalTaskHandler '%s'. Current value: %d %s. " +
                            "Please provide a value greater than or equal to 0.", handlerClassName, metadata.initialDelay(), metadata.timeUnit())
            );
        }
    }

    private List<String> getProcessDefinitionKeys(ExternalTaskSubscription annotation) {
        var processDefinitionKeys = annotation.processDefinitionKeyIn();

        return processDefinitionKeys != null && processDefinitionKeys.length > 0
                ? List.of(processDefinitionKeys)
                : List.of(annotation.processDefinitionKey());
    }

    private String getHandlerClassName(WorkerContext context) {
        return context.handler().getClass().getSimpleName();
    }

}
