package com.leorces.rest.client;

import com.leorces.model.runtime.activity.ActivityFailure;
import com.leorces.rest.client.client.TaskRestClient;
import com.leorces.rest.client.model.ExternalTask;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;

@Slf4j
@AllArgsConstructor
@Service("leorcesExternalTaskService")
public class ExternalTaskServiceImpl implements ExternalTaskService {

    private final TaskRestClient taskRestClient;

    @Override
    public boolean complete(ExternalTask externalTask) {
        return complete(externalTask, Collections.emptyMap());
    }

    @Override
    public boolean complete(ExternalTask externalTask, Map<String, Object> variables) {
        try {
            var response = taskRestClient.complete(externalTask.id(), Objects.requireNonNullElse(variables, Map.of()));
            var isSuccessful = response.getStatusCode().is2xxSuccessful();
            if (!isSuccessful) {
                log.warn("ExternalTask completion failed with status: {} for taskId: {}",
                        response.getStatusCode(), externalTask.id());
            }
            return isSuccessful;
        } catch (Exception e) {
            log.error("Exception during externalTask completion for taskId: {}", externalTask.id(), e);
            return false;
        }
    }

    @Override
    public boolean fail(String taskId) {
        return fail(taskId, null, Collections.emptyMap());
    }

    @Override
    public boolean fail(String taskId, Map<String, Object> variables) {
        return fail(taskId, null, variables);
    }

    @Override
    public boolean fail(String taskId, ActivityFailure failure) {
        return fail(taskId, failure, Collections.emptyMap());
    }

    @Override
    public boolean fail(String taskId, ActivityFailure failure, Map<String, Object> variables) {
        try {
            var response = taskRestClient.fail(taskId, failure, Objects.requireNonNullElse(variables, Map.of()));
            var isSuccessful = response.getStatusCode().is2xxSuccessful();
            if (!isSuccessful) {
                log.warn("ExternalTask failure operation failed with status: {} for taskId: {}",
                        response.getStatusCode(), taskId);
            }
            return isSuccessful;
        } catch (Exception e) {
            log.error("Exception during task failure for taskId: {}", taskId, e);
            return false;
        }
    }

}
