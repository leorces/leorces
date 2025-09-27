package com.leorces.engine;

import com.leorces.api.RuntimeService;
import com.leorces.engine.event.EngineEventBus;
import com.leorces.engine.event.correlation.CorrelationEvent;
import com.leorces.engine.process.ProcessStartService;
import com.leorces.engine.variables.VariableRuntimeService;
import com.leorces.model.runtime.process.Process;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Map;

@Slf4j
@Service
@AllArgsConstructor
public class RuntimeServiceImpl implements RuntimeService {

    private final VariableRuntimeService variableRuntimeService;
    private final ProcessStartService processStartService;
    private final EngineEventBus eventBus;

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
        log.debug("Start process by definition id: {} with business key: {} and variables: {}", definitionId, businessKey, variables);
        return processStartService.startByDefinitionId(definitionId, businessKey, variables);
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
        log.debug("Start process by key: {} with business key: {} and variables: {}", key, businessKey, variables);
        return processStartService.startByDefinitionKey(key, businessKey, variables);
    }

    @Override
    public void setVariable(String executionId, String key, Object value) {
        setVariables(executionId, Collections.singletonMap(key, value));
    }

    @Override
    public void setVariables(String executionId, Map<String, Object> variables) {
        log.debug("Set variables: {} for execution id: {}", variables, executionId);
        variableRuntimeService.setVariables(executionId, variables);
    }

    @Override
    public void setVariableLocal(String executionId, String key, Object value) {
        setVariablesLocal(executionId, Collections.singletonMap(key, value));
    }

    @Override
    public void setVariablesLocal(String executionId, Map<String, Object> variables) {
        log.debug("Set local variables: {} for execution id: {}", variables, executionId);
        variableRuntimeService.setVariablesLocal(executionId, variables);
    }

    @Override
    public void correlateMessage(String messageName, String businessKey) {
        correlateMessage(messageName, businessKey, Map.of(), Map.of());
    }

    @Override
    public void correlateMessage(String messageName, Map<String, Object> correlationKeys) {
        correlateMessage(messageName, null, correlationKeys, Map.of());
    }

    @Override
    public void correlateMessage(String messageName, String businessKey, Map<String, Object> processVariables) {
        correlateMessage(messageName, businessKey, Map.of(), processVariables);
    }

    @Override
    public void correlateMessage(String messageName, Map<String, Object> correlationKeys, Map<String, Object> processVariables) {
        correlateMessage(messageName, null, correlationKeys, processVariables);
    }

    @Override
    public void correlateMessage(String messageName, String businessKey, Map<String, Object> correlationKeys, Map<String, Object> processVariables) {
        log.debug("Correlate message: {} for business key: {} and process variables: {} and correlation keys: {}", messageName, businessKey, processVariables, correlationKeys);
        eventBus.publish(CorrelationEvent.message(messageName, businessKey, correlationKeys, processVariables));
    }

}
