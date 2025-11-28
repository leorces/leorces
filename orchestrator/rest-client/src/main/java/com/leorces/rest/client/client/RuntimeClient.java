package com.leorces.rest.client.client;

import com.leorces.model.runtime.process.Process;
import com.leorces.model.search.ProcessFilter;
import com.leorces.rest.client.model.request.CorrelateMessageRequest;
import com.leorces.rest.client.model.request.ProcessModificationRequest;
import com.leorces.rest.client.model.request.StartProcessByIdRequest;
import com.leorces.rest.client.model.request.StartProcessByKeyRequest;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
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
public class RuntimeClient {

    private final RestClient leorcesRestClient;

    public RuntimeClient(@Qualifier("leorcesRestClient") RestClient leorcesRestClient) {
        this.leorcesRestClient = leorcesRestClient;
    }

    @Retry(name = "start-process")
    @CircuitBreaker(name = "start-process", fallbackMethod = "startProcessFallback")
    public Process startProcessByKey(String definitionKey, String businessKey, Map<String, Object> variables) {
        try {
            return leorcesRestClient.post()
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

    @Retry(name = "start-process")
    @CircuitBreaker(name = "start-process", fallbackMethod = "startProcessFallback")
    public Process startProcessById(String definitionId, String businessKey, Map<String, Object> variables) {
        try {
            return leorcesRestClient.post()
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

    @Retry(name = "terminate-process")
    @CircuitBreaker(name = "terminate-process", fallbackMethod = "terminateProcessFallback")
    public void terminateProcess(String processId) {
        try {
            leorcesRestClient.put()
                    .uri(TERMINATE_PROCESS_BY_ID_ENDPOINT.formatted(processId))
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .toBodilessEntity();
        } catch (HttpClientErrorException.BadRequest e) {
            log.warn("Bad request for terminate process by id: processId={}, error={}", processId, e.getMessage());
        } catch (HttpClientErrorException.NotFound e) {
            log.warn("Process not found: processId={}", processId);
        } catch (HttpServerErrorException.InternalServerError e) {
            log.error("Server error during process termination by id: processId={}, error={}", processId, e.getMessage());
            throw e;
        } catch (HttpServerErrorException.ServiceUnavailable e) {
            log.error("Service unavailable during process termination by id: processId={}, error={}", processId, e.getMessage());
            throw e;
        } catch (ResourceAccessException e) {
            log.error("Connection error during process termination by id: processId={}, error={}", processId, e.getMessage());
            throw e;
        }
    }

    @Retry(name = "resolve-incident")
    @CircuitBreaker(name = "resolve-incident", fallbackMethod = "resolveIncidentFallback")
    public void resolveIncident(String processId) {
        try {
            leorcesRestClient.put()
                    .uri(RESOLVE_INCIDENT_BY_PROCESS_ID.formatted(processId))
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .toBodilessEntity();
        } catch (HttpClientErrorException.BadRequest e) {
            log.warn("Bad request for resolve incident by process id: processId={}, error={}", processId, e.getMessage());
        } catch (HttpClientErrorException.NotFound e) {
            log.warn("Process not found: processId={}", processId);
        } catch (HttpServerErrorException.InternalServerError e) {
            log.error("Server error during resolving incident by process id: processId={}, error={}", processId, e.getMessage());
            throw e;
        } catch (HttpServerErrorException.ServiceUnavailable e) {
            log.error("Service unavailable during resolving incident by process id: processId={}, error={}", processId, e.getMessage());
            throw e;
        } catch (ResourceAccessException e) {
            log.error("Connection error during resolving incident by process id: processId={}, error={}", processId, e.getMessage());
            throw e;
        }
    }

    @Retry(name = "move-execution")
    @CircuitBreaker(name = "move-execution", fallbackMethod = "moveExecutionFallback")
    public void moveExecution(String processId, String activityId, String targetDefinitionId) {
        try {
            leorcesRestClient.put()
                    .uri(MODIFY_PROCESS_BY_ID_ENDPOINT.formatted(processId))
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .body(new ProcessModificationRequest(activityId, targetDefinitionId))
                    .retrieve()
                    .toBodilessEntity();
        } catch (HttpClientErrorException.BadRequest e) {
            log.warn("Bad request to modify process by id: processId={}, error={}", processId, e.getMessage());
        } catch (HttpClientErrorException.NotFound e) {
            log.warn("Process not found: processId={}", processId);
        } catch (HttpServerErrorException.InternalServerError e) {
            log.error("Server error during process modification by id: processId={}, error={}", processId, e.getMessage());
            throw e;
        } catch (HttpServerErrorException.ServiceUnavailable e) {
            log.error("Service unavailable during process modification by id: processId={}, error={}", processId, e.getMessage());
            throw e;
        } catch (ResourceAccessException e) {
            log.error("Connection error during process modification by id: processId={}, error={}", processId, e.getMessage());
            throw e;
        }
    }

    @Retry(name = "runtime-correlate")
    @CircuitBreaker(name = "runtime-correlate", fallbackMethod = "correlateMessageFallback")
    public void correlateMessage(String message, String businessKey, Map<String, Object> correlationKeys, Map<String, Object> processVariables) {
        try {
            leorcesRestClient.put()
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
            leorcesRestClient.put()
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
            leorcesRestClient.put()
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

    @Retry(name = "find-process")
    @CircuitBreaker(name = "find-process", fallbackMethod = "findProcessFallback")
    public Process findProcess(ProcessFilter filter) {
        try {
            return leorcesRestClient.post()
                    .uri(FIND_PROCESS)
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .body(filter)
                    .retrieve()
                    .body(Process.class);
        } catch (HttpClientErrorException.BadRequest e) {
            log.warn("Bad request for find process by filter={}, error={}", filter, e.getMessage());
            throw e;
        } catch (HttpClientErrorException.NotFound e) {
            log.warn("Process not found by filter={}", filter);
            return null;
        } catch (HttpServerErrorException.InternalServerError e) {
            log.error("Server error during finding process by filter={}, error={}", filter, e.getMessage());
            throw e;
        } catch (HttpServerErrorException.ServiceUnavailable e) {
            log.error("Service unavailable during finding process by filter={}, error={}", filter, e.getMessage());
            throw e;
        } catch (ResourceAccessException e) {
            log.error("Connection error during finding process by filter={}, error={}", filter, e.getMessage());
            throw e;
        }
    }

    private void terminateProcessFallback(String processId, Exception e) {
        if (e instanceof HttpServerErrorException || e instanceof ResourceAccessException) {
            log.error("Service unavailable for terminate process: processId={}, error={}", processId, e.getMessage());
        } else if (e instanceof HttpClientErrorException clientError) {
            log.warn("Client error for terminate process: processId={}, status={}, error={}", processId, clientError.getStatusCode(), e.getMessage());
        } else {
            log.error("Unexpected error for terminate process: processId={}", processId, e);
        }
    }

    private void resolveIncidentFallback(String processId, Exception e) {
        if (e instanceof HttpServerErrorException || e instanceof ResourceAccessException) {
            log.error("Service unavailable for resolving incidents in process: processId={}, error={}", processId, e.getMessage());
        } else if (e instanceof HttpClientErrorException clientError) {
            log.warn("Client error for resolving incidents in process: processId={}, status={}, error={}", processId, clientError.getStatusCode(), e.getMessage());
        } else {
            log.error("Unexpected error for resolving incidents in process: processId={}", processId, e);
        }
    }

    private void moveExecutionFallback(String processId, String activityId, String targetDefinitionId, Exception e) {
        if (e instanceof HttpServerErrorException || e instanceof ResourceAccessException) {
            log.error("Service unavailable for process modification: processId={}, error={}", processId, e.getMessage());
        } else if (e instanceof HttpClientErrorException clientError) {
            log.warn("Client error for process modification: processId={}, status={}, error={}", processId, clientError.getStatusCode(), e.getMessage());
        } else {
            log.error("Unexpected error for process modification: processId={}", processId, e);
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

    private void findProcessFallback(ProcessFilter filter, Exception e) {
        if (e instanceof HttpServerErrorException || e instanceof ResourceAccessException) {
            log.error("Service unavailable for finding process by filter={}, error={}", filter, e.getMessage());
        } else if (e instanceof HttpClientErrorException clientError) {
            log.warn("Client error for finding process by filter={}, status={}, error={}",
                    filter, clientError.getStatusCode(), e.getMessage());
        } else {
            log.error("Unexpected error for finding process by filter={}, variables={}", filter, e.getMessage());
        }
    }

}
