package com.leorces.engine;

import com.leorces.api.RuntimeService;
import com.leorces.engine.activity.command.RetryAllActivitiesCommand;
import com.leorces.engine.core.CommandDispatcher;
import com.leorces.engine.correlation.command.CorrelateMessageCommand;
import com.leorces.engine.process.command.*;
import com.leorces.engine.variables.command.SetVariablesCommand;
import com.leorces.model.runtime.process.Process;
import com.leorces.model.search.ProcessFilter;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Map;

@Slf4j
@AllArgsConstructor
@Service("leorcesRuntimeService")
public class RuntimeServiceImpl implements RuntimeService {

    private final CommandDispatcher dispatcher;

    @Override
    public Process startProcessById(String definitionId) {
        return startProcessById(definitionId, null, Map.of());
    }

    @Override
    public Process startProcessById(String definitionId,
                                    Map<String, Object> variables) {
        return startProcessById(definitionId, null, variables);
    }

    @Override
    public Process startProcessById(String definitionId,
                                    String businessKey) {
        return startProcessById(definitionId, businessKey, Map.of());
    }

    @Override
    public Process startProcessById(String definitionId,
                                    String businessKey,
                                    Map<String, Object> variables) {
        log.debug("Start process by definition id: {} with business key: {} and variables: {}", definitionId, businessKey, variables);
        return dispatcher.execute(RunProcessCommand.byDefinitionId(definitionId, businessKey, variables));
    }

    @Override
    public Process startProcessByKey(String key) {
        return startProcessByKey(key, null, Map.of());
    }

    @Override
    public Process startProcessByKey(String key,
                                     Map<String, Object> variables) {
        return startProcessByKey(key, null, variables);
    }

    @Override
    public Process startProcessByKey(String key,
                                     String businessKey) {
        return startProcessByKey(key, businessKey, Map.of());
    }

    @Override
    public Process startProcessByKey(String key,
                                     String businessKey,
                                     Map<String, Object> variables) {
        log.debug("Start process by key: {} with business key: {} and variables: {}", key, businessKey, variables);
        return dispatcher.execute(RunProcessCommand.byDefinitionKey(key, businessKey, variables));
    }

    @Override
    public void terminateProcess(String processId) {
        log.debug("Terminate process by process id: {}", processId);
        dispatcher.dispatch(TerminateProcessCommand.of(processId));
    }

    @Override
    public void resolveIncident(String processId) {
        log.debug("Resolve incident by process id: {}", processId);
        dispatcher.dispatch(RetryAllActivitiesCommand.of(processId));
    }

    @Override
    public void suspendProcessById(String processId) {
        log.debug("Suspend process by process id: {}", processId);
        dispatcher.dispatch(SuspendProcessCommand.ofProcessId(processId));
    }

    @Override
    public void suspendProcessesByDefinitionId(String definitionId) {
        log.debug("Suspend processes by definition id: {}", definitionId);
        dispatcher.dispatch(SuspendProcessCommand.ofDefinitionId(definitionId));
    }

    @Override
    public void suspendProcessesByDefinitionKey(String definitionKey) {
        log.debug("Suspend processes by definition key: {}", definitionKey);
        dispatcher.dispatch(SuspendProcessCommand.ofDefinitionKey(definitionKey));
    }

    @Override
    public void resumeProcessById(String processId) {
        log.debug("Resume process by process id: {}", processId);
        dispatcher.dispatch(ResumeProcessCommand.ofProcessId(processId));
    }

    @Override
    public void resumeProcessesByDefinitionId(String definitionId) {
        log.debug("Resume processes by definition id: {}", definitionId);
        dispatcher.dispatch(ResumeProcessCommand.ofDefinitionId(definitionId));
    }

    @Override
    public void resumeProcessesByDefinitionKey(String definitionKey) {
        log.debug("Resume processes by definition key: {}", definitionKey);
        dispatcher.dispatch(ResumeProcessCommand.ofDefinitionKey(definitionKey));
    }

    @Override
    public Process findProcess(ProcessFilter filter) {
        log.debug("Find process by filter: {}", filter);
        return dispatcher.execute(new FindProcessByFilterCommand(filter));
    }

    @Override
    public void moveExecution(String processId, String activityId, String targetDefinitionId) {
        log.debug("Move execution from: {} to: {}", activityId, targetDefinitionId);
        dispatcher.dispatch(MoveExecutionCommand.of(processId, activityId, targetDefinitionId));
    }

    @Override
    public void setVariable(String executionId,
                            String key,
                            Object value) {
        setVariables(executionId, Collections.singletonMap(key, value));
    }

    @Override
    public void setVariables(String executionId,
                             Map<String, Object> variables) {
        log.debug("Set variables: {} for execution id: {}", variables, executionId);
        dispatcher.dispatch(SetVariablesCommand.of(executionId, variables, false));
    }

    @Override
    public void setVariableLocal(String executionId,
                                 String key,
                                 Object value) {
        setVariablesLocal(executionId, Collections.singletonMap(key, value));
    }

    @Override
    public void setVariablesLocal(String executionId,
                                  Map<String, Object> variables) {
        log.debug("Set local variables: {} for execution id: {}", variables, executionId);
        dispatcher.dispatch(SetVariablesCommand.of(executionId, variables, true));
    }

    @Override
    public void correlateMessage(String messageName,
                                 String businessKey) {
        correlateMessage(messageName, businessKey, Map.of(), Map.of());
    }

    @Override
    public void correlateMessage(String messageName,
                                 Map<String, Object> correlationKeys) {
        correlateMessage(messageName, null, correlationKeys, Map.of());
    }

    @Override
    public void correlateMessage(String messageName,
                                 String businessKey,
                                 Map<String, Object> processVariables) {
        correlateMessage(messageName, businessKey, Map.of(), processVariables);
    }

    @Override
    public void correlateMessage(String messageName,
                                 Map<String, Object> correlationKeys,
                                 Map<String, Object> processVariables) {
        correlateMessage(messageName, null, correlationKeys, processVariables);
    }

    @Override
    public void correlateMessage(String messageName,
                                 String businessKey,
                                 Map<String, Object> correlationKeys,
                                 Map<String, Object> processVariables) {
        log.debug("Correlate message: {} for business key: {} and process variables: {} and correlation keys: {}", messageName, businessKey, processVariables, correlationKeys);
        dispatcher.dispatch(CorrelateMessageCommand.of(messageName, businessKey, correlationKeys, processVariables));
    }

}
