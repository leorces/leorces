package com.leorces.rest.client.service;

import com.leorces.model.runtime.activity.ActivityFailure;
import com.leorces.rest.client.model.Task;

import java.util.Map;

public interface TaskService {

    boolean complete(Task task);

    boolean complete(Task task, Map<String, Object> variables);

    boolean fail(String taskId);

    boolean fail(String taskId, Map<String, Object> variables);

    boolean fail(String taskId, ActivityFailure failure);

    boolean fail(String taskId, ActivityFailure failure, Map<String, Object> variables);

}
