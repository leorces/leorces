package com.leorces.example.client.worker.delivery;

import com.leorces.rest.client.ExternalTaskService;
import com.leorces.rest.client.handler.ExternalTaskHandler;
import com.leorces.rest.client.model.ExternalTask;
import com.leorces.rest.client.worker.ExternalTaskSubscription;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@Component
@ExternalTaskSubscription(
        topicName = "notification",
        processDefinitionKey = "OrderDeliveryProcess"
)
public class DeliveryNotificationWorker implements ExternalTaskHandler {

    @Override
    @SneakyThrows
    public void execute(ExternalTask externalTask, ExternalTaskService externalTaskService) {
        var message = externalTask.getVariable("message");
        log.debug("Sending notification: {}", message);
        Thread.sleep(ThreadLocalRandom.current().nextLong(500, 2001));
        externalTaskService.complete(externalTask);
    }

}
