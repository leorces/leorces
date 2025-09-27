package com.leorces.example.client.worker.delivery;

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
        topic = "postal-delivery",
        processDefinitionKey = "OrderDeliveryProcess"
)
public class PostalDeliveryWorker implements TaskHandler {

    @Override
    @SneakyThrows
    public void handle(Task task, TaskService taskService) {
        log.debug("Postal delivery selected");
        Thread.sleep(ThreadLocalRandom.current().nextLong(500, 2001));
        taskService.complete(task);
    }

}
