package com.leorces.rest.client.worker;

import com.leorces.rest.client.exception.ClientConfigurationValidationException;
import com.leorces.rest.client.handler.TaskHandler;
import com.leorces.rest.client.model.worker.WorkerContext;
import lombok.AllArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class TaskWorkerInitializer implements ApplicationListener<ApplicationReadyEvent> {

    private final WorkerScheduler scheduler;
    private final WorkerConfigResolver workerConfigResolver;
    private final ApplicationContext applicationContext;

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        var taskHandlerBeans = applicationContext.getBeansOfType(TaskHandler.class);

        taskHandlerBeans.values().forEach(handler -> {
            var annotation = handler.getClass().getAnnotation(TaskWorker.class);
            if (annotation != null) {
                var metadata = workerConfigResolver.resolveWorkerConfig(annotation);
                var context = WorkerContext.create(handler, metadata);
                validate(context);
                scheduler.startWorker(context);
            }
        });
    }

    private void validate(WorkerContext context) {
        var metadata = context.metadata();
        var handlerClassName = context.handler().getClass().getSimpleName();

        if (metadata.topic().isBlank()) {
            throw new ClientConfigurationValidationException(
                    String.format("Topic cannot be blank for TaskHandler '%s'. Current value: '%s'. " +
                            "Please provide a non-empty topic name.", handlerClassName, metadata.topic())
            );
        }
        if (metadata.processDefinitionKey().isBlank()) {
            throw new ClientConfigurationValidationException(
                    String.format("ProcessDefinitionKey cannot be blank for TaskHandler '%s'. Current value: '%s'. " +
                            "Please provide a non-empty process definition key.", handlerClassName, metadata.processDefinitionKey())
            );
        }
        if (metadata.interval() <= 0) {
            throw new ClientConfigurationValidationException(
                    String.format("Interval must be positive for TaskHandler '%s'. Current value: %d %s. " +
                            "Please provide a value greater than 0.", handlerClassName, metadata.interval(), metadata.timeUnit())
            );
        }
        if (metadata.maxConcurrentTasks() <= 0) {
            throw new ClientConfigurationValidationException(
                    String.format("MaxConcurrentTasks must be positive for TaskHandler '%s'. Current value: %d. " +
                            "Please provide a value greater than 0.", handlerClassName, metadata.maxConcurrentTasks())
            );
        }
        if (metadata.initialDelay() < 0) {
            throw new ClientConfigurationValidationException(
                    String.format("InitialDelay must be non-negative for TaskHandler '%s'. Current value: %d %s. " +
                            "Please provide a value greater than or equal to 0.", handlerClassName, metadata.initialDelay(), metadata.timeUnit())
            );
        }
    }
}
