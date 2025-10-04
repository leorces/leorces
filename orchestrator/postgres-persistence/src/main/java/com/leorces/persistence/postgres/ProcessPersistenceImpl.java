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
import com.leorces.persistence.postgres.utils.ProcessStateTransition;
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
        var newProcess = save(ProcessStateTransition.to(ProcessState.ACTIVE).apply(process), true);
        var newVariables = variablePersistence.save(newProcess);
        return newProcess.toBuilder()
                .variables(newVariables)
                .build();
    }

    @Override
    @Transactional
    public Process complete(Process process) {
        return save(ProcessStateTransition.to(ProcessState.COMPLETED).apply(process), false);
    }

    @Override
    @Transactional
    public Process terminate(Process process) {
        return save(ProcessStateTransition.to(ProcessState.TERMINATED).apply(process), false);
    }

    @Override
    @Transactional
    public Process incident(Process process) {
        return save(ProcessStateTransition.to(ProcessState.INCIDENT).apply(process), false);
    }


    @Override
    @Transactional
    public void changeState(String processId, ProcessState state) {
        processRepository.changeState(processId, state.name());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Process> findById(String processId) {
        return processRepository.findById(processId)
                .map(processMapper::toProcess);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ProcessExecution> findExecutionById(String processId) {
        return processRepository.findByIdWithActivities(processId)
                .map(processMapper::toExecution);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Process> findByBusinessKey(String businessKey) {
        return processRepository.findAllByBusinessKey(businessKey).stream()
                .map(processMapper::toProcess)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Process> findByVariables(Map<String, Object> variables) {
        if (variables == null || variables.isEmpty()) {
            return Collections.emptyList();
        }

        var variableKeys = extractVariableKeys(variables);
        var variableValues = extractVariableValues(variables);
        return processRepository.findByVariables(variableKeys, variableValues, variables.size()).stream()
                .map(processMapper::toProcess)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Process> findByBusinessKeyAndVariables(String businessKey, Map<String, Object> variables) {
        if (variables == null || variables.isEmpty()) {
            return Collections.emptyList();
        }

        var variableKeys = extractVariableKeys(variables);
        var variableValues = extractVariableValues(variables);
        return processRepository.findByBusinessKeyAndVariables(businessKey, variableKeys, variableValues, variables.size()).stream()
                .map(processMapper::toProcess)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProcessExecution> findAllFullyCompleted(int limit) {
        return processRepository.findAllFullyCompleted(limit).stream()
                .map(processMapper::toExecution)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PageableData<Process> findAll(Pageable pageable) {
        var result = processRepository.findAll(pageable);
        return new PageableData<>(processMapper.toProcesses(result.data()), result.total());
    }

    private Process save(Process process, boolean isNew) {
        var entity = processMapper.toEntity(process, isNew);
        var newEntity = processRepository.save(entity);
        return process.toBuilder()
                .id(newEntity.getId())
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
