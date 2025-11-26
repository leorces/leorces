package com.leorces.rest.client.handler;

import com.leorces.rest.client.model.ExternalTask;
import com.leorces.rest.client.service.ExternalTaskService;

public interface ExternalTaskHandler {

    void doExecute(ExternalTask externalTask, ExternalTaskService externalTaskService);

}
