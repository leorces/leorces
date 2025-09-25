package com.leorces.rest.client.handler;

import com.leorces.rest.client.model.Task;
import com.leorces.rest.client.service.TaskService;

public interface TaskHandler {

    void handle(Task task, TaskService taskService);

}
