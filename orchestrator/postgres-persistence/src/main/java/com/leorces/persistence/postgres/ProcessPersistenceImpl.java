package com.leorces.persistence.postgres;

import com.leorces.model.pagination.Pageable;
import com.leorces.model.pagination.PageableData;
import com.leorces.model.runtime.process.Process;
import com.leorces.model.runtime.process.ProcessExecution;
import com.leorces.model.runtime.process.ProcessState;
import com.leorces.persistence.ProcessPersistence;
import com.leorces.persistence.VariablePersistence;
import com.leorces.persistence.postgres.mapper.ProcessMapper;
import com.leorces.persistence.postgres.repository.ProcessRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@AllArgsConstructor
public class ProcessPersistenceImpl implements ProcessPersistence {

    private final VariablePersistence variablePersistence;
    private final ProcessRepository processRepository;
    private final ProcessMapper processMapper;

    @Override
    @Transactional
    public Process run(Process process) {
        log.debug("Run process: {}", process.definitionKey());
        var newProcess = save(process);
        var newVariables = variablePersistence.save(newProcess);
        return newProcess.toBuilder()
                .variables(newVariables)
                .build();
    }

    @Override
    public void complete(String processId) {
        log.debug("Complete process: {}", processId);
        processRepository.complete(processId);
    }

    @Override
    public void terminate(String processId) {
        log.debug("Terminate process: {}", processId);
        processRepository.terminate(processId);
    }

    @Override
    public void incident(String processId) {
        log.debug("Incident process: {}", processId);
        processRepository.incident(processId);
    }

    @Override
    public void changeState(String processId, ProcessState state) {
        log.debug("Change process: {} state to: {}", processId, state);
        processRepository.changeState(processId, state.name());
    }

    @Override
    public Optional<Process> findById(String processId) {
        log.debug("Finding process by id: {}", processId);
        return processRepository.findById(processId)
                .map(processMapper::toProcess);
    }

    @Override
    public Optional<ProcessExecution> findExecutionById(String processId) {
        log.debug("Finding process execution by id: {}", processId);
        return processRepository.findByIdWithActivities(processId)
                .map(processMapper::toExecution);
    }

    @Override
    public List<Process> findByBusinessKey(String businessKey) {
        log.debug("Finding all processes by business key: {}", businessKey);
        return processRepository.findAllByBusinessKey(businessKey).stream()
                .map(processMapper::toProcess)
                .toList();
    }

    @Override
    public List<Process> findByVariables(Map<String, Object> variables) {
        if (variables == null || variables.isEmpty()) {
            return Collections.emptyList();
        }

        log.debug("Finding all processes by variables: {}", variables);
        var variableKeys = extractVariableKeys(variables);
        var variableValues = extractVariableValues(variables);
        return processRepository.findByVariables(variableKeys, variableValues, variables.size()).stream()
                .map(processMapper::toProcess)
                .toList();
    }

    @Override
    public List<Process> findByBusinessKeyAndVariables(String businessKey, Map<String, Object> variables) {
        if (variables == null || variables.isEmpty()) {
            return Collections.emptyList();
        }

        log.debug("Finding all processes by business key: {} and variables: {}", businessKey, variables);
        var variableKeys = extractVariableKeys(variables);
        var variableValues = extractVariableValues(variables);
        return processRepository.findByBusinessKeyAndVariables(businessKey, variableKeys, variableValues, variables.size()).stream()
                .map(processMapper::toProcess)
                .toList();
    }

    @Override
    public List<ProcessExecution> findAllFullyCompleted(int limit) {
        log.debug("Finding all fully completed processes with limit: {}", limit);
        return processRepository.findAllFullyCompleted(limit).stream()
                .map(processMapper::toExecution)
                .toList();
    }

    @Override
    public PageableData<Process> findAll(Pageable pageable) {
        log.debug("Finding all processes with pageable: {}", pageable);
        var result = processRepository.findAll(pageable);
        return new PageableData<>(processMapper.toProcesses(result.data()), result.total());
    }

    private Process save(Process process) {
        var entity = processMapper.toNewEntity(process);
        var newEntity = processRepository.save(entity);
        return process.toBuilder()
                .id(newEntity.getId())
                .businessKey(newEntity.getBusinessKey())
                .state(ProcessState.valueOf(newEntity.getState()))
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .startedAt(entity.getStartedAt())
                .completedAt(entity.getCompletedAt())
                .build();
    }

    private String[] extractVariableKeys(Map<String, Object> variables) {
        return variables.keySet().toArray(new String[0]);
    }

    private String[] extractVariableValues(Map<String, Object> variables) {
        return variables.values().stream()
                .map(String::valueOf)
                .toArray(String[]::new);
    }

}
