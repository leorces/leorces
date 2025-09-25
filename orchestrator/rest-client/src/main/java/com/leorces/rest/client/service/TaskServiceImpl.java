package com.leorces.rest.client.service;

import com.leorces.rest.client.client.TaskRestClient;
import com.leorces.rest.client.model.Task;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Map;

@Slf4j
@Service
@AllArgsConstructor
public class TaskServiceImpl implements TaskService {

    private final TaskRestClient taskRestClient;

    @Override
    public boolean complete(Task task) {
        return complete(task, Collections.emptyMap());
    }

    @Override
    public boolean complete(Task task, Map<String, Object> variables) {
        try {
            var response = taskRestClient.complete(task.id(), variables);
            var isSuccessful = response.getStatusCode().is2xxSuccessful();
            if (!isSuccessful) {
                log.warn("Task completion failed with status: {} for taskId: {}",
                        response.getStatusCode(), task.id());
            }
            return isSuccessful;
        } catch (Exception e) {
            log.error("Exception during task completion for taskId: {}", task.id(), e);
            return false;
        }
    }

    @Override
    public boolean fail(String taskId) {
        return fail(taskId, Collections.emptyMap());
    }

    @Override
    public boolean fail(String taskId, Map<String, Object> variables) {
        try {
            var response = taskRestClient.fail(taskId, variables);
            var isSuccessful = response.getStatusCode().is2xxSuccessful();
            if (!isSuccessful) {
                log.warn("Task failure operation failed with status: {} for taskId: {}",
                        response.getStatusCode(), taskId);
            }
            return isSuccessful;
        } catch (Exception e) {
            log.error("Exception during task failure for taskId: {}", taskId, e);
            return false;
        }
    }
}
