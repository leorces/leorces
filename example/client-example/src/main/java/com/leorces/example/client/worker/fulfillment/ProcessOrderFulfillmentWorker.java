package com.leorces.example.client.worker.fulfillment;

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
        topic = "process-order-fulfillment",
        processDefinitionKey = "OrderFulfillmentProcess"
)
public class ProcessOrderFulfillmentWorker implements TaskHandler {

    @Override
    @SneakyThrows
    public void handle(Task task, TaskService taskService) {
        log.debug("Order fulfillment in progress");
        Thread.sleep(ThreadLocalRandom.current().nextLong(500, 2001));
        taskService.complete(task);
    }

}
