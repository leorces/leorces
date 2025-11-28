package com.leorces.rest.client.service;

import com.leorces.api.RuntimeService;
import com.leorces.model.runtime.process.Process;
import com.leorces.model.search.ProcessFilter;
import com.leorces.rest.client.client.RuntimeClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@Service("leorcesRuntimeService")
public class RuntimeServiceImpl implements RuntimeService {

    private final RuntimeClient runtimeClient;

    @Override
    public Process startProcessById(String definitionId) {
        return startProcessById(definitionId, null, Map.of());
    }

    @Override
    public Process startProcessById(String definitionId, Map<String, Object> variables) {
        return startProcessById(definitionId, null, variables);
    }

    @Override
    public Process startProcessById(String definitionId, String businessKey) {
        return startProcessById(definitionId, businessKey, Map.of());
    }

    @Override
    public Process startProcessById(String definitionId, String businessKey, Map<String, Object> variables) {
        return runtimeClient.startProcessById(definitionId, businessKey, variables);
    }

    @Override
    public Process startProcessByKey(String key) {
        return startProcessByKey(key, null, Map.of());
    }

    @Override
    public Process startProcessByKey(String key, Map<String, Object> variables) {
        return startProcessByKey(key, null, variables);
    }

    @Override
    public Process startProcessByKey(String key, String businessKey) {
        return startProcessByKey(key, businessKey, Map.of());
    }

    @Override
    public Process startProcessByKey(String key, String businessKey, Map<String, Object> variables) {
        return runtimeClient.startProcessByKey(key, businessKey, variables);
    }

    @Override
    public void terminateProcess(String processId) {
        runtimeClient.terminateProcess(processId);
    }

    @Override
    public void resolveIncident(String processId) {
        runtimeClient.resolveIncident(processId);
    }

    @Override
    public Process findProcess(ProcessFilter filter) {
        return runtimeClient.findProcess(filter);
    }

    @Override
    public void moveExecution(String processId, String activityId, String targetDefinitionId) {
        runtimeClient.moveExecution(processId, activityId, targetDefinitionId);
    }

    @Override
    public void setVariable(String executionId, String key, Object value) {
        setVariables(executionId, Map.of(key, value));
    }

    @Override
    public void setVariables(String executionId, Map<String, Object> variables) {
        runtimeClient.setVariables(executionId, variables);
    }

    @Override
    public void setVariableLocal(String executionId, String key, Object value) {
        setVariablesLocal(executionId, Map.of(key, value));
    }

    @Override
    public void setVariablesLocal(String executionId, Map<String, Object> variables) {
        runtimeClient.setVariablesLocal(executionId, variables);
    }

    @Override
    public void correlateMessage(String messageName, String businessKey) {
        correlateMessage(messageName, businessKey, Map.of(), Map.of());
    }

    @Override
    public void correlateMessage(String messageName, Map<String, Object> correlationKeys) {
        correlateMessage(messageName, null, Map.of(), correlationKeys);
    }

    @Override
    public void correlateMessage(String messageName, String businessKey, Map<String, Object> processVariables) {
        correlateMessage(messageName, businessKey, processVariables, Map.of());
    }

    @Override
    public void correlateMessage(String messageName, Map<String, Object> correlationKeys, Map<String, Object> processVariables) {
        correlateMessage(messageName, null, processVariables, correlationKeys);
    }

    @Override
    public void correlateMessage(String messageName, String businessKey, Map<String, Object> processVariables, Map<String, Object> correlationKeys) {
        runtimeClient.correlateMessage(messageName, businessKey, processVariables, correlationKeys);
    }

}
