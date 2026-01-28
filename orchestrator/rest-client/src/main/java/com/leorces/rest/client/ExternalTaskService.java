package com.leorces.rest.client;

import com.leorces.model.runtime.activity.ActivityFailure;
import com.leorces.rest.client.model.ExternalTask;

import java.util.Map;

public interface ExternalTaskService {

    boolean complete(ExternalTask externalTask);

    boolean complete(ExternalTask externalTask, Map<String, Object> variables);

    boolean fail(String taskId);

    boolean fail(String taskId, Map<String, Object> variables);

    boolean fail(String taskId, ActivityFailure failure);

    boolean fail(String taskId, ActivityFailure failure, Map<String, Object> variables);

}
