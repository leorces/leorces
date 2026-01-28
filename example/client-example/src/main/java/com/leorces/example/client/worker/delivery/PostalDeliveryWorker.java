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
        topicName = "postal-delivery",
        processDefinitionKey = "OrderDeliveryProcess"
)
public class PostalDeliveryWorker implements ExternalTaskHandler {

    @Override
    @SneakyThrows
    public void execute(ExternalTask externalTask, ExternalTaskService externalTaskService) {
        log.debug("Postal delivery selected");
        Thread.sleep(ThreadLocalRandom.current().nextLong(500, 2001));
        externalTaskService.complete(externalTask);
    }

}
