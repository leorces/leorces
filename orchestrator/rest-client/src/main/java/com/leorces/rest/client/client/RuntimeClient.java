package com.leorces.rest.client.client;

import com.leorces.model.runtime.process.Process;
import com.leorces.model.search.ProcessFilter;
import com.leorces.rest.client.model.request.CorrelateMessageRequest;
import com.leorces.rest.client.model.request.ProcessModificationRequest;
import com.leorces.rest.client.model.request.StartProcessByIdRequest;
import com.leorces.rest.client.model.request.StartProcessByKeyRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
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

    public Process startProcessByKey(String definitionKey, String businessKey, Map<String, Object> variables) {
        try {
            return leorcesRestClient.post()
                    .uri(START_PROCESS_BY_KEY_ENDPOINT)
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .body(new StartProcessByKeyRequest(definitionKey, businessKey, variables))
                    .retrieve()
                    .body(Process.class);
        } catch (Exception e) {
            log.warn("Can't start process by key: definitionKey={}, error={}", definitionKey, e.getMessage());
            throw e;
        }
    }

    public Process startProcessById(String definitionId, String businessKey, Map<String, Object> variables) {
        try {
            return leorcesRestClient.post()
                    .uri(START_PROCESS_BY_ID_ENDPOINT)
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .body(new StartProcessByIdRequest(definitionId, businessKey, variables))
                    .retrieve()
                    .body(Process.class);
        } catch (Exception e) {
            log.warn("Can't start process by id: definitionId={}, error={}", definitionId, e.getMessage());
            throw e;
        }
    }

    public void terminateProcess(String processId) {
        try {
            leorcesRestClient.put()
                    .uri(TERMINATE_PROCESS_BY_ID_ENDPOINT.formatted(processId))
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .toBodilessEntity();
        } catch (Exception e) {
            log.warn("Can't terminate process by id: processId={}, error={}", processId, e.getMessage());
            throw e;
        }
    }

    public void resolveIncident(String processId) {
        try {
            leorcesRestClient.put()
                    .uri(RESOLVE_INCIDENT_BY_PROCESS_ID.formatted(processId))
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .toBodilessEntity();
        } catch (Exception e) {
            log.warn("Can't resolve incident by process id: processId={}, error={}", processId, e.getMessage());
            throw e;
        }
    }

    public void moveExecution(String processId, String activityId, String targetDefinitionId) {
        try {
            leorcesRestClient.put()
                    .uri(MODIFY_PROCESS_BY_ID_ENDPOINT.formatted(processId))
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .body(new ProcessModificationRequest(activityId, targetDefinitionId))
                    .retrieve()
                    .toBodilessEntity();
        } catch (Exception e) {
            log.warn("Can't modify process by id: processId={}, error={}", processId, e.getMessage());
            throw e;
        }
    }

    public void correlateMessage(String messageName, String businessKey, Map<String, Object> correlationKeys, Map<String, Object> processVariables) {
        var request = CorrelateMessageRequest.builder()
                .message(messageName)
                .businessKey(businessKey)
                .correlationKeys(correlationKeys)
                .processVariables(processVariables)
                .build();
        try {
            leorcesRestClient.put()
                    .uri(CORRELATE_MESSAGE_ENDPOINT)
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .toBodilessEntity();
        } catch (Exception e) {
            log.warn("Can't correlate message: message={}, error={}", messageName, e.getMessage());
            throw e;
        }
    }

    public void setVariables(String executionId, Map<String, Object> variables) {
        try {
            leorcesRestClient.put()
                    .uri(SET_VARIABLES_ENDPOINT.formatted(executionId))
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .body(variables)
                    .retrieve()
                    .toBodilessEntity();
        } catch (Exception e) {
            log.warn("Can't set variables: executionId={}, error={}", executionId, e.getMessage());
            throw e;
        }
    }

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
            log.warn("Can't set local variables: executionId={}, error={}", executionId, e.getMessage());
            throw e;
        }
    }

    public Process findProcess(ProcessFilter filter) {
        try {
            return leorcesRestClient.post()
                    .uri(FIND_PROCESS)
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .body(filter)
                    .retrieve()
                    .body(Process.class);
        } catch (HttpClientErrorException.NotFound e) {
            log.debug("Process not found by filter={}", filter);
            return null;
        } catch (HttpClientErrorException.BadRequest e) {
            log.warn("Can't find process by filter={}, error={}", filter, e.getMessage());
            throw e;
        }
    }

}
