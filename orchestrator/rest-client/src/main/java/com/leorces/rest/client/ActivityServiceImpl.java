package com.leorces.rest.client;

import com.leorces.api.ActivityService;
import com.leorces.model.runtime.activity.Activity;
import com.leorces.model.runtime.activity.ActivityFailure;
import com.leorces.rest.client.client.ActivityClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@Service("leorcesActivityService")
public class ActivityServiceImpl implements ActivityService {

    private final ActivityClient activityClient;

    @Override
    public void run(String activityDefinitionId, String processId) {
        activityClient.run(processId, activityDefinitionId);
    }

    @Override
    public void complete(String activityId) {
        complete(activityId, Map.of());
    }

    @Override
    public void complete(String activityId, Map<String, Object> variables) {
        activityClient.complete(activityId, variables);
    }

    @Override
    public void fail(String activityId) {
        fail(activityId, null, Map.of());
    }

    @Override
    public void fail(String activityId, Map<String, Object> variables) {
        fail(activityId, null, variables);
    }

    @Override
    public void fail(String activityId, ActivityFailure failure) {
        fail(activityId, failure, Map.of());
    }

    @Override
    public void fail(String activityId, ActivityFailure failure, Map<String, Object> variables) {
        activityClient.fail(activityId, failure, variables);
    }

    @Override
    public void terminate(String activityId) {
        activityClient.terminate(activityId);
    }

    @Override
    public void retry(String activityId) {
        activityClient.retry(activityId);
    }

    @Override
    public List<Activity> poll(String topic, String processDefinitionKey, int limit) {
        return activityClient.poll(processDefinitionKey, topic, limit);
    }

}
