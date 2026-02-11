package com.leorces.persistence.postgres;

import com.leorces.model.runtime.activity.ActivityExecution;
import com.leorces.model.runtime.process.Process;
import com.leorces.model.runtime.variable.Variable;
import com.leorces.persistence.VariablePersistence;
import com.leorces.persistence.postgres.mapper.VariableMapper;
import com.leorces.persistence.postgres.repository.VariableRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@AllArgsConstructor
public class VariablePersistenceImpl implements VariablePersistence {

    private final VariableRepository variableRepository;
    private final VariableMapper variableMapper;

    @Override
    public List<Variable> save(Process process) {
        log.debug("Save variables for process: {}", process.id());
        var entities = variableMapper.toEntities(process);
        variableRepository.saveAll(entities);
        return variableMapper.toVariables(entities);
    }

    @Override
    public List<Variable> save(ActivityExecution activity) {
        log.debug("Save variables for activity: {}", activity.id());
        var entities = variableMapper.toEntities(activity);
        variableRepository.saveAll(entities);
        return variableMapper.toVariables(entities);
    }

    @Override
    public List<Variable> update(List<Variable> variables) {
        log.debug("Update variables: {}", variables);
        var variableEntities = variableMapper.toEntities(variables);
        variableRepository.saveAll(variableEntities);
        return variableMapper.toVariables(variableEntities);
    }

    @Override
    public List<Variable> findInScope(String processId, List<String> scope) {
        log.debug("Finding variables in scope for process: {} and scope: {}", processId, scope);
        var variableEntities = variableRepository.findInScope(processId, scope.toArray(String[]::new));
        return variableMapper.toVariables(variableEntities);
    }

    @Override
    public List<Variable> findInProcess(String processId) {
        log.debug("Finding variables in process: {}", processId);
        var variableEntities = variableRepository.findInProcess(processId);
        return variableMapper.toVariables(variableEntities);
    }

    public void updateDefinitionId(String definitionId, List<String> processIds) {
        log.debug("Updating definition id to: {} for {} processes", definitionId, processIds.size());
        variableRepository.updateDefinitionId(definitionId, processIds.toArray(String[]::new));
    }

    public void deleteByExecutionId(String executionId) {
        log.debug("Deleting variables by execution id: {}", executionId);
        variableRepository.deleteByExecutionId(executionId);
    }

    public void deleteByExecutionIds(List<String> executionIds) {
        log.debug("Deleting variables by execution ids: {}", executionIds);
        variableRepository.deleteByExecutionIds(executionIds.toArray(String[]::new));
    }

}
