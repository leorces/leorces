package com.leorces.example.client.worker.submit;

import com.leorces.rest.client.handler.TaskHandler;
import com.leorces.rest.client.model.Task;
import com.leorces.rest.client.service.TaskService;
import com.leorces.rest.client.worker.TaskWorker;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@Component
@TaskWorker(
        topic = "notification",
        processDefinitionKey = "OrderSubmittedProcess"
)
public class OrderSubmittedNotificationWorker implements TaskHandler {

    @Override
    @SneakyThrows
    public void handle(Task task, TaskService taskService) {
        var message = task.getVariable("message");
        log.debug("Sending notification: {}", message);
        Thread.sleep(ThreadLocalRandom.current().nextLong(500, 2001));
        taskService.complete(task);
    }

}
