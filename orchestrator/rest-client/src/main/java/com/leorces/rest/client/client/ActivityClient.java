package com.leorces.rest.client.client;

import com.leorces.model.runtime.activity.Activity;
import com.leorces.model.runtime.activity.ActivityFailure;
import com.leorces.rest.client.model.request.FailActivityRequest;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.leorces.rest.client.constants.ApiConstants.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class ActivityClient {

    private static final ParameterizedTypeReference<List<Activity>> ACTIVITY_LIST_TYPE_REF = new ParameterizedTypeReference<>() {
    };

    private final RestClient restClient;

    @Retry(name = "activity-run")
    @CircuitBreaker(name = "activity-run", fallbackMethod = "runFallback")
    public void run(String processId, String activityDefinitionId) {
        try {
            restClient.put()
                    .uri(RUN_ACTIVITY_ENDPOINT.formatted(processId, activityDefinitionId))
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .toBodilessEntity();
        } catch (HttpClientErrorException.BadRequest e) {
            log.warn("Bad request for run activity: processId={}, activityDefinitionId={}, error={}",
                    processId, activityDefinitionId, e.getMessage());
        } catch (HttpClientErrorException.NotFound e) {
            log.warn("Activity or process not found for run: processId={}, activityDefinitionId={}",
                    processId, activityDefinitionId);
        } catch (HttpServerErrorException.InternalServerError e) {
            log.error("Server error during run activity: processId={}, activityDefinitionId={}, error={}",
                    processId, activityDefinitionId, e.getMessage());
            throw e;
        } catch (HttpServerErrorException.ServiceUnavailable e) {
            log.error("Service unavailable during run activity: processId={}, activityDefinitionId={}, error={}",
                    processId, activityDefinitionId, e.getMessage());
            throw e;
        } catch (ResourceAccessException e) {
            log.error("Connection error during run activity: processId={}, activityDefinitionId={}, error={}",
                    processId, activityDefinitionId, e.getMessage());
            throw e;
        }
    }

    @Retry(name = "activity-complete")
    @CircuitBreaker(name = "activity-complete", fallbackMethod = "completeFallback")
    public void complete(String activityId, Map<String, Object> variables) {
        try {
            restClient.put()
                    .uri(COMPLETE_ACTIVITY_ENDPOINT.formatted(activityId))
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .body(variables)
                    .retrieve()
                    .toBodilessEntity();
        } catch (HttpClientErrorException.BadRequest e) {
            log.warn("Bad request for complete activity: activityId={}, error={}", activityId, e.getMessage());
        } catch (HttpClientErrorException.NotFound e) {
            log.warn("Activity not found for completion: activityId={}", activityId);
        } catch (HttpServerErrorException.InternalServerError e) {
            log.error("Server error during complete activity: activityId={}, error={}", activityId, e.getMessage());
            throw e;
        } catch (HttpServerErrorException.ServiceUnavailable e) {
            log.error("Service unavailable during complete activity: activityId={}, error={}", activityId, e.getMessage());
            throw e;
        } catch (ResourceAccessException e) {
            log.error("Connection error during complete activity: activityId={}, error={}", activityId, e.getMessage());
            throw e;
        }
    }

    @Retry(name = "activity-fail")
    @CircuitBreaker(name = "activity-fail", fallbackMethod = "failFallback")
    public void fail(String activityId, ActivityFailure failure, Map<String, Object> variables) {
        try {
            restClient.put()
                    .uri(FAIL_ACTIVITY_ENDPOINT.formatted(activityId))
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .body(new FailActivityRequest(failure, variables))
                    .retrieve()
                    .toBodilessEntity();
        } catch (HttpClientErrorException.BadRequest e) {
            log.warn("Bad request for fail activity: activityId={}, error={}", activityId, e.getMessage());
        } catch (HttpClientErrorException.NotFound e) {
            log.warn("Activity not found for failure: activityId={}", activityId);
        } catch (HttpServerErrorException.InternalServerError e) {
            log.error("Server error during fail activity: activityId={}, error={}", activityId, e.getMessage());
            throw e;
        } catch (HttpServerErrorException.ServiceUnavailable e) {
            log.error("Service unavailable during fail activity: activityId={}, error={}", activityId, e.getMessage());
            throw e;
        } catch (ResourceAccessException e) {
            log.error("Connection error during fail activity: activityId={}, error={}", activityId, e.getMessage());
            throw e;
        }
    }

    @Retry(name = "activity-terminate")
    @CircuitBreaker(name = "activity-terminate", fallbackMethod = "terminateFallback")
    public void terminate(String activityId) {
        try {
            restClient.put()
                    .uri(TERMINATE_ACTIVITY_ENDPOINT.formatted(activityId))
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .toBodilessEntity();
        } catch (HttpClientErrorException.BadRequest e) {
            log.warn("Bad request for terminate activity: activityId={}, error={}", activityId, e.getMessage());
        } catch (HttpClientErrorException.NotFound e) {
            log.warn("Activity not found for termination: activityId={}", activityId);
        } catch (HttpServerErrorException.InternalServerError e) {
            log.error("Server error during terminate activity: activityId={}, error={}", activityId, e.getMessage());
            throw e;
        } catch (HttpServerErrorException.ServiceUnavailable e) {
            log.error("Service unavailable during terminate activity: activityId={}, error={}", activityId, e.getMessage());
            throw e;
        } catch (ResourceAccessException e) {
            log.error("Connection error during terminate activity: activityId={}, error={}", activityId, e.getMessage());
            throw e;
        }
    }

    @Retry(name = "activity-retry")
    @CircuitBreaker(name = "activity-retry", fallbackMethod = "retryFallback")
    public void retry(String activityId) {
        try {
            restClient.put()
                    .uri(RETRY_ACTIVITY_ENDPOINT.formatted(activityId))
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .toBodilessEntity();
        } catch (HttpClientErrorException.BadRequest e) {
            log.warn("Bad request for retry activity: activityId={}, error={}", activityId, e.getMessage());
        } catch (HttpClientErrorException.NotFound e) {
            log.warn("Activity not found for retry: activityId={}", activityId);
        } catch (HttpServerErrorException.InternalServerError e) {
            log.error("Server error during retry activity: activityId={}, error={}", activityId, e.getMessage());
            throw e;
        } catch (HttpServerErrorException.ServiceUnavailable e) {
            log.error("Service unavailable during retry activity: activityId={}, error={}", activityId, e.getMessage());
            throw e;
        } catch (ResourceAccessException e) {
            log.error("Connection error during retry activity: activityId={}, error={}", activityId, e.getMessage());
            throw e;
        }
    }

    @Retry(name = "activity-poll")
    @CircuitBreaker(name = "activity-poll", fallbackMethod = "pollFallback")
    public List<Activity> poll(String processDefinitionKey, String topic, int limit) {
        try {
            return restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path(POLL_ACTIVITIES_ENDPOINT.formatted(processDefinitionKey, topic))
                            .queryParam(SIZE_PARAM, limit)
                            .build())
                    .retrieve()
                    .body(ACTIVITY_LIST_TYPE_REF);
        } catch (HttpClientErrorException.NotFound e) {
            log.debug("No activities found for poll: processDefinitionKey={}, topic={}", processDefinitionKey, topic);
            return Collections.emptyList();
        } catch (HttpClientErrorException.BadRequest e) {
            log.warn("Bad request for poll activities: processDefinitionKey={}, topic={}, error={}",
                    processDefinitionKey, topic, e.getMessage());
            return Collections.emptyList();
        } catch (HttpServerErrorException.InternalServerError e) {
            log.error("Server error during poll activities: processDefinitionKey={}, topic={}, error={}",
                    processDefinitionKey, topic, e.getMessage());
            throw e;
        } catch (HttpServerErrorException.ServiceUnavailable e) {
            log.error("Service unavailable during poll activities: processDefinitionKey={}, topic={}, error={}",
                    processDefinitionKey, topic, e.getMessage());
            throw e;
        } catch (ResourceAccessException e) {
            log.error("Connection error during poll activities: processDefinitionKey={}, topic={}, error={}",
                    processDefinitionKey, topic, e.getMessage());
            throw e;
        }
    }

    private void runFallback(String processId, String activityDefinitionId, Exception e) {
        if (e instanceof HttpServerErrorException || e instanceof ResourceAccessException) {
            log.error("Service unavailable for run activity: processId={}, activityDefinitionId={}, error={}",
                    processId, activityDefinitionId, e.getMessage());
        } else if (e instanceof HttpClientErrorException clientError) {
            log.warn("Client error for run activity: processId={}, activityDefinitionId={}, status={}, error={}",
                    processId, activityDefinitionId, clientError.getStatusCode(), e.getMessage());
        } else {
            log.error("Unexpected error for run activity: processId={}, activityDefinitionId={}",
                    processId, activityDefinitionId, e);
        }
    }

    private void completeFallback(String activityId, Map<String, Object> variables, Exception e) {
        if (e instanceof HttpServerErrorException || e instanceof ResourceAccessException) {
            log.error("Service unavailable for complete activity: activityId={}, error={}", activityId, e.getMessage());
        } else if (e instanceof HttpClientErrorException clientError) {
            log.warn("Client error for complete activity: activityId={}, status={}, error={}",
                    activityId, clientError.getStatusCode(), e.getMessage());
        } else {
            log.error("Unexpected error for complete activity: activityId={}, variables: {}", activityId, variables, e);
        }
    }

    private void failFallback(String activityId, Map<String, Object> variables, Exception e) {
        if (e instanceof HttpServerErrorException || e instanceof ResourceAccessException) {
            log.error("Service unavailable for fail activity: activityId={}, error={}", activityId, e.getMessage());
        } else if (e instanceof HttpClientErrorException clientError) {
            log.warn("Client error for fail activity: activityId={}, status={}, error={}",
                    activityId, clientError.getStatusCode(), e.getMessage());
        } else {
            log.error("Unexpected error for fail activity: activityId={}, variables: {}", activityId, variables, e);
        }
    }

    private void terminateFallback(String activityId, Exception e) {
        if (e instanceof HttpServerErrorException || e instanceof ResourceAccessException) {
            log.error("Service unavailable for terminate activity: activityId={}, error={}", activityId, e.getMessage());
        } else if (e instanceof HttpClientErrorException clientError) {
            log.warn("Client error for terminate activity: activityId={}, status={}, error={}",
                    activityId, clientError.getStatusCode(), e.getMessage());
        } else {
            log.error("Unexpected error for terminate activity: activityId={}", activityId, e);
        }
    }

    private void retryFallback(String activityId, Exception e) {
        if (e instanceof HttpServerErrorException || e instanceof ResourceAccessException) {
            log.error("Service unavailable for retry activity: activityId={}, error={}", activityId, e.getMessage());
        } else if (e instanceof HttpClientErrorException clientError) {
            log.warn("Client error for retry activity: activityId={}, status={}, error={}",
                    activityId, clientError.getStatusCode(), e.getMessage());
        } else {
            log.error("Unexpected error for retry activity: activityId={}", activityId, e);
        }
    }

    private List<Activity> pollFallback(String processDefinitionKey, String topic, int limit, Exception e) {
        log.warn("Failed to poll activities for processDefinitionKey: {}, topic: {}, limit: {}. Fallback response",
                processDefinitionKey, topic, limit, e);
        return Collections.emptyList();
    }

}
