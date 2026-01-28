package com.leorces.rest.client.handler;

import com.leorces.rest.client.ExternalTaskService;
import com.leorces.rest.client.model.ExternalTask;

public interface ExternalTaskHandler {

    void execute(ExternalTask externalTask, ExternalTaskService externalTaskService);

}
