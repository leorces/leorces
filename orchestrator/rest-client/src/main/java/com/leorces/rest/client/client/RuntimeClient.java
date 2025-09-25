package com.leorces.rest.client.client;

import com.leorces.model.runtime.process.Process;
import com.leorces.rest.client.model.request.CorrelateMessageRequest;
import com.leorces.rest.client.model.request.StartProcessByIdRequest;
import com.leorces.rest.client.model.request.StartProcessByKeyRequest;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

import java.util.Map;

import static com.leorces.rest.client.constants.ApiConstants.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class RuntimeClient {

    private final RestClient restClient;

    @Retry(name = "runtime-process")
    @CircuitBreaker(name = "runtime-process", fallbackMethod = "startProcessFallback")
    public Process startProcessByKey(String definitionKey, String businessKey, Map<String, Object> variables) {
        try {
            return restClient.post()
                    .uri(START_PROCESS_BY_KEY_ENDPOINT)
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .body(new StartProcessByKeyRequest(definitionKey, businessKey, variables))
                    .retrieve()
                    .body(Process.class);
        } catch (HttpClientErrorException.BadRequest e) {
            log.warn("Bad request for start process by key: definitionKey={}, error={}", definitionKey, e.getMessage());
            return null;
        } catch (HttpClientErrorException.NotFound e) {
            log.warn("Process definition not found: definitionKey={}", definitionKey);
            return null;
        } catch (HttpServerErrorException.InternalServerError e) {
            log.error("Server error during start process by key: definitionKey={}, error={}", definitionKey, e.getMessage());
            throw e;
        } catch (HttpServerErrorException.ServiceUnavailable e) {
            log.error("Service unavailable during start process by key: definitionKey={}, error={}", definitionKey, e.getMessage());
            throw e;
        } catch (ResourceAccessException e) {
            log.error("Connection error during start process by key: definitionKey={}, error={}", definitionKey, e.getMessage());
            throw e;
        }
    }

    @Retry(name = "runtime-process")
    @CircuitBreaker(name = "runtime-process", fallbackMethod = "startProcessFallback")
    public Process startProcessById(String definitionId, String businessKey, Map<String, Object> variables) {
        try {
            return restClient.post()
                    .uri(START_PROCESS_BY_ID_ENDPOINT)
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .body(new StartProcessByIdRequest(definitionId, businessKey, variables))
                    .retrieve()
                    .body(Process.class);
        } catch (HttpClientErrorException.BadRequest e) {
            log.warn("Bad request for start process by id: definitionId={}, error={}", definitionId, e.getMessage());
            return null;
        } catch (HttpClientErrorException.NotFound e) {
            log.warn("Process definition not found: definitionId={}", definitionId);
            return null;
        } catch (HttpServerErrorException.InternalServerError e) {
            log.error("Server error during start process by id: definitionId={}, error={}", definitionId, e.getMessage());
            throw e;
        } catch (HttpServerErrorException.ServiceUnavailable e) {
            log.error("Service unavailable during start process by id: definitionId={}, error={}", definitionId, e.getMessage());
            throw e;
        } catch (ResourceAccessException e) {
            log.error("Connection error during start process by id: definitionId={}, error={}", definitionId, e.getMessage());
            throw e;
        }
    }

    @Retry(name = "runtime-correlate")
    @CircuitBreaker(name = "runtime-correlate", fallbackMethod = "correlateMessageFallback")
    public void correlateMessage(String message, String businessKey, Map<String, Object> correlationKeys, Map<String, Object> processVariables) {
        try {
            restClient.put()
                    .uri(CORRELATE_MESSAGE_ENDPOINT)
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .body(new CorrelateMessageRequest(message, businessKey, correlationKeys, processVariables))
                    .retrieve()
                    .toBodilessEntity();
        } catch (HttpClientErrorException.BadRequest e) {
            log.warn("Bad request for correlate message: message={}, error={}", message, e.getMessage());
        } catch (HttpServerErrorException.InternalServerError e) {
            log.error("Server error during correlate message: message={}, error={}", message, e.getMessage());
            throw e;
        } catch (HttpServerErrorException.ServiceUnavailable e) {
            log.error("Service unavailable during correlate message: message={}, error={}", message, e.getMessage());
            throw e;
        } catch (ResourceAccessException e) {
            log.error("Connection error during correlate message: message={}, error={}", message, e.getMessage());
            throw e;
        }
    }

    @Retry(name = "runtime-variables")
    @CircuitBreaker(name = "runtime-variables", fallbackMethod = "setVariablesFallback")
    public void setVariables(String executionId, Map<String, Object> variables) {
        try {
            restClient.put()
                    .uri(SET_VARIABLES_ENDPOINT.formatted(executionId))
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .body(variables)
                    .retrieve()
                    .toBodilessEntity();
        } catch (HttpClientErrorException.BadRequest e) {
            log.warn("Bad request for set variables: executionId={}, error={}", executionId, e.getMessage());
        } catch (HttpClientErrorException.NotFound e) {
            log.warn("Execution not found: executionId={}", executionId);
        } catch (HttpServerErrorException.InternalServerError e) {
            log.error("Server error during set variables: executionId={}, error={}", executionId, e.getMessage());
            throw e;
        } catch (HttpServerErrorException.ServiceUnavailable e) {
            log.error("Service unavailable during set variables: executionId={}, error={}", executionId, e.getMessage());
            throw e;
        } catch (ResourceAccessException e) {
            log.error("Connection error during set variables: executionId={}, error={}", executionId, e.getMessage());
            throw e;
        }
    }

    @Retry(name = "runtime-variables")
    @CircuitBreaker(name = "runtime-variables", fallbackMethod = "setVariablesFallback")
    public void setVariablesLocal(String executionId, Map<String, Object> variables) {
        try {
            restClient.put()
                    .uri(SET_VARIABLES_LOCAL_ENDPOINT.formatted(executionId))
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .body(variables)
                    .retrieve()
                    .toBodilessEntity();
        } catch (HttpClientErrorException.BadRequest e) {
            log.warn("Bad request for set local variables: executionId={}, error={}", executionId, e.getMessage());
        } catch (HttpClientErrorException.NotFound e) {
            log.warn("Execution not found for local variables: executionId={}", executionId);
        } catch (HttpServerErrorException.InternalServerError e) {
            log.error("Server error during set local variables: executionId={}, error={}", executionId, e.getMessage());
            throw e;
        } catch (HttpServerErrorException.ServiceUnavailable e) {
            log.error("Service unavailable during set local variables: executionId={}, error={}", executionId, e.getMessage());
            throw e;
        } catch (ResourceAccessException e) {
            log.error("Connection error during set local variables: executionId={}, error={}", executionId, e.getMessage());
            throw e;
        }
    }

    private Process startProcessFallback(String definition, String businessKey, Map<String, Object> variables, Exception e) {
        if (e instanceof HttpServerErrorException || e instanceof ResourceAccessException) {
            log.error("Service unavailable for start process: definition={}, error={}", definition, e.getMessage());
        } else if (e instanceof HttpClientErrorException clientError) {
            log.warn("Client error for start process: definition={}, status={}, error={}",
                    definition, clientError.getStatusCode(), e.getMessage());
        } else {
            log.error("Unexpected error for start process: definition={}, variables: {}", definition, variables, e);
        }
        return null;
    }

    private void correlateMessageFallback(String message, String businessKey, Map<String, Object> correlationKeys, Map<String, Object> processVariables, Exception e) {
        if (e instanceof HttpServerErrorException || e instanceof ResourceAccessException) {
            log.error("Service unavailable for correlate message: message={}, error={}", message, e.getMessage());
        } else if (e instanceof HttpClientErrorException clientError) {
            log.warn("Client error for correlate message: message={}, status={}, error={}",
                    message, clientError.getStatusCode(), e.getMessage());
        } else {
            log.error("Unexpected error for correlate message: message={}", message, e);
        }
    }

    private void setVariablesFallback(String executionId, Map<String, Object> variables, Exception e) {
        if (e instanceof HttpServerErrorException || e instanceof ResourceAccessException) {
            log.error("Service unavailable for set variables: executionId={}, error={}", executionId, e.getMessage());
        } else if (e instanceof HttpClientErrorException clientError) {
            log.warn("Client error for set variables: executionId={}, status={}, error={}",
                    executionId, clientError.getStatusCode(), e.getMessage());
        } else {
            log.error("Unexpected error for set variables: executionId={}, variables: {}", executionId, variables, e);
        }
    }
}
