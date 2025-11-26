package com.leorces.example.client.worker.fulfillment;

import com.leorces.rest.client.handler.ExternalTaskHandler;
import com.leorces.rest.client.model.ExternalTask;
import com.leorces.rest.client.service.ExternalTaskService;
import com.leorces.rest.client.worker.ExternalTaskSubscription;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@Component
@ExternalTaskSubscription(
        topicName = "process-order-fulfillment",
        processDefinitionKey = "OrderFulfillmentProcess"
)
public class ProcessOrderFulfillmentWorker implements ExternalTaskHandler {

    @Override
    @SneakyThrows
    public void doExecute(ExternalTask externalTask, ExternalTaskService externalTaskService) {
        log.debug("Order fulfillment in progress");
        Thread.sleep(ThreadLocalRandom.current().nextLong(500, 2001));
        externalTaskService.complete(externalTask);
    }

}
