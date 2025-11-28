package com.leorces.rest.client.client;

import com.leorces.model.runtime.activity.Activity;
import com.leorces.model.runtime.activity.ActivityFailure;
import com.leorces.rest.client.model.request.FailActivityRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.leorces.rest.client.constants.ApiConstants.*;

@Slf4j
@Component
public class ActivityClient {

    private static final ParameterizedTypeReference<List<Activity>> ACTIVITY_LIST_TYPE_REF = new ParameterizedTypeReference<>() {
    };

    private final RestClient leorcesRestClient;

    public ActivityClient(@Qualifier("leorcesRestClient") RestClient leorcesRestClient) {
        this.leorcesRestClient = leorcesRestClient;
    }

    public void run(String processId, String activityDefinitionId) {
        try {
            leorcesRestClient.put()
                    .uri(RUN_ACTIVITY_ENDPOINT.formatted(processId, activityDefinitionId))
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .toBodilessEntity();
        } catch (Exception e) {
            log.warn("Can't run activity: processId={}, activityDefinitionId={}, error={}", processId, activityDefinitionId, e.getMessage());
            throw e;
        }
    }

    public void complete(String activityId, Map<String, Object> variables) {
        try {
            leorcesRestClient.put()
                    .uri(COMPLETE_ACTIVITY_ENDPOINT.formatted(activityId))
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .body(variables)
                    .retrieve()
                    .toBodilessEntity();
        } catch (Exception e) {
            log.warn("Can't complete activity: activityId={}, error={}", activityId, e.getMessage());
            throw e;
        }
    }

    public void fail(String activityId, ActivityFailure failure, Map<String, Object> variables) {
        try {
            leorcesRestClient.put()
                    .uri(FAIL_ACTIVITY_ENDPOINT.formatted(activityId))
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .body(new FailActivityRequest(failure, variables))
                    .retrieve()
                    .toBodilessEntity();
        } catch (Exception e) {
            log.warn("Can't fail activity: activityId={}, error={}", activityId, e.getMessage());
            throw e;
        }
    }

    public void terminate(String activityId) {
        try {
            leorcesRestClient.put()
                    .uri(TERMINATE_ACTIVITY_ENDPOINT.formatted(activityId))
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .toBodilessEntity();
        } catch (Exception e) {
            log.warn("Can't terminate activity: activityId={}, error={}", activityId, e.getMessage());
            throw e;
        }
    }

    public void retry(String activityId) {
        try {
            leorcesRestClient.put()
                    .uri(RETRY_ACTIVITY_ENDPOINT.formatted(activityId))
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .toBodilessEntity();
        } catch (Exception e) {
            log.warn("Can't retry activity: activityId={}, error={}", activityId, e.getMessage());
            throw e;
        }
    }

    public List<Activity> poll(String processDefinitionKey, String topic, int limit) {
        try {
            return leorcesRestClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path(POLL_ACTIVITIES_ENDPOINT.formatted(processDefinitionKey, topic))
                            .queryParam(SIZE_PARAM, limit)
                            .build())
                    .retrieve()
                    .body(ACTIVITY_LIST_TYPE_REF);
        } catch (HttpClientErrorException.NotFound e) {
            log.debug("No activities found for poll: processDefinitionKey={}, topic={}", processDefinitionKey, topic);
            return Collections.emptyList();
        } catch (Exception e) {
            log.warn("Can't poll activities: processDefinitionKey={}, topic={}, error={}", processDefinitionKey, topic, e.getMessage());
            throw e;
        }
    }

}
