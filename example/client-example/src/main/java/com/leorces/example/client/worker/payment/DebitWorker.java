package com.leorces.example.client.worker.payment;

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
        topicName = "debit",
        processDefinitionKey = "OrderPaymentProcess"
)
public class DebitWorker implements ExternalTaskHandler {

    @Override
    @SneakyThrows
    public void execute(ExternalTask externalTask, ExternalTaskService externalTaskService) {
        var amount = externalTask.getVariable("amount");
        log.debug("Try to debit: {}", amount);
        Thread.sleep(ThreadLocalRandom.current().nextLong(500, 2001));
        externalTaskService.complete(externalTask);
    }

}
