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
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@AllArgsConstructor
public class VariablePersistenceImpl implements VariablePersistence {

    private final VariableRepository variableRepository;
    private final VariableMapper variableMapper;

    @Override
    @Transactional
    public List<Variable> save(Process process) {
        var entities = variableMapper.toEntities(process);
        variableRepository.saveAll(entities);
        return variableMapper.toVariables(entities);
    }

    @Override
    @Transactional
    public List<Variable> save(ActivityExecution activity) {
        var entities = variableMapper.toEntities(activity);
        variableRepository.saveAll(entities);
        return variableMapper.toVariables(entities);
    }

    @Override
    @Transactional
    public List<Variable> update(List<Variable> variables) {
        var variableEntities = variableMapper.toEntities(variables);
        variableRepository.saveAll(variableEntities);
        return variableMapper.toVariables(variableEntities);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Variable> findAll(String processId, List<String> scope) {
        var variableEntities = variableRepository.findAll(processId, scope);
        return variableMapper.toVariables(variableEntities);
    }

}
