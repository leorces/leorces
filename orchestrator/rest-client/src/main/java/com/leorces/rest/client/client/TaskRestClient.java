package com.leorces.rest.client.client;

import com.leorces.rest.client.model.Task;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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
@AllArgsConstructor
public class TaskRestClient {

    private static final ParameterizedTypeReference<List<Task>> TASK_LIST_TYPE_REF = new ParameterizedTypeReference<>() {
    };

    private final RestClient restClient;


    @Retry(name = "task-update")
    @CircuitBreaker(name = "task-update", fallbackMethod = "updateFallback")
    public ResponseEntity<Void> complete(String taskId, Map<String, Object> variables) {
        try {
            return restClient.put()
                    .uri(COMPLETE_ACTIVITY_ENDPOINT.formatted(taskId))
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .body(variables)
                    .retrieve()
                    .toBodilessEntity();
        } catch (HttpClientErrorException.BadRequest e) {
            log.warn("Bad request for task completion: taskId={}, error={}", taskId, e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (HttpClientErrorException.NotFound e) {
            log.warn("Task not found for completion: taskId={}", taskId);
            return ResponseEntity.notFound().build();
        } catch (HttpClientErrorException.Conflict e) {
            log.warn("Task conflict for completion: taskId={}, error={}", taskId, e.getMessage());
            return ResponseEntity.status(409).build();
        } catch (HttpServerErrorException.InternalServerError e) {
            log.error("Server error during task completion: taskId={}, error={}", taskId, e.getMessage());
            throw e;
        } catch (HttpServerErrorException.ServiceUnavailable e) {
            log.error("Service unavailable during task completion: taskId={}, error={}", taskId, e.getMessage());
            throw e;
        } catch (ResourceAccessException e) {
            log.error("Connection error during task completion: taskId={}, error={}", taskId, e.getMessage());
            throw e;
        }
    }


    @Retry(name = "task-update")
    @CircuitBreaker(name = "task-update", fallbackMethod = "updateFallback")
    public ResponseEntity<Void> fail(String taskId, Map<String, Object> variables) {
        try {
            return restClient.put()
                    .uri(FAIL_ACTIVITY_ENDPOINT.formatted(taskId))
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .body(variables)
                    .retrieve()
                    .toBodilessEntity();
        } catch (HttpClientErrorException.BadRequest e) {
            log.warn("Bad request for task failure: taskId={}, error={}", taskId, e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (HttpClientErrorException.NotFound e) {
            log.warn("Task not found for failure: taskId={}", taskId);
            return ResponseEntity.notFound().build();
        } catch (HttpClientErrorException.Conflict e) {
            log.warn("Task conflict for failure: taskId={}, error={}", taskId, e.getMessage());
            return ResponseEntity.status(409).build();
        } catch (HttpServerErrorException.InternalServerError e) {
            log.error("Server error during task failure: taskId={}, error={}", taskId, e.getMessage());
            throw e;
        } catch (HttpServerErrorException.ServiceUnavailable e) {
            log.error("Service unavailable during task failure: taskId={}, error={}", taskId, e.getMessage());
            throw e;
        } catch (ResourceAccessException e) {
            log.error("Connection error during task failure: taskId={}, error={}", taskId, e.getMessage());
            throw e;
        }
    }

    @CircuitBreaker(name = "task-poll", fallbackMethod = "pollFallback")
    public ResponseEntity<List<Task>> poll(String topic, String processDefinitionKey, int size) {
        try {
            return restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path(POLL_ACTIVITIES_ENDPOINT.formatted(processDefinitionKey, topic))
                            .queryParam(SIZE_PARAM, size)
                            .build())
                    .retrieve()
                    .toEntity(TASK_LIST_TYPE_REF);
        } catch (HttpClientErrorException.NotFound e) {
            return ResponseEntity.ok(Collections.emptyList());
        }
    }

    private ResponseEntity<Void> updateFallback(String taskId, Map<String, Object> variables, Exception e) {
        if (e instanceof HttpServerErrorException || e instanceof ResourceAccessException) {
            log.error("Service unavailable for task update: taskId={}, error={}", taskId, e.getMessage());
            return ResponseEntity.status(503).build();
        } else if (e instanceof HttpClientErrorException clientError) {
            log.warn("Client error for task update: taskId={}, status={}, error={}",
                    taskId, clientError.getStatusCode(), e.getMessage());
            return ResponseEntity.status(clientError.getStatusCode()).build();
        } else {
            log.error("Unexpected error for task update: taskId={}, variables: {}", taskId, variables, e);
            return ResponseEntity.status(500).build();
        }
    }

    private ResponseEntity<List<Task>> pollFallback(String topic, String processDefinitionKey, int size, Exception e) {
        log.warn("Failed to poll tasks for topic: {}, processDefinitionKey: {}, limit:{}. Fallback response", topic, processDefinitionKey, size, e);
        return ResponseEntity.ok(Collections.emptyList());
    }

}
