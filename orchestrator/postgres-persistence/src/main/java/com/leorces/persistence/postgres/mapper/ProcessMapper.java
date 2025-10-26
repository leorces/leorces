package com.leorces.persistence.postgres.mapper;

import com.leorces.model.runtime.process.Process;
import com.leorces.model.runtime.process.ProcessExecution;
import com.leorces.model.runtime.process.ProcessState;
import com.leorces.persistence.postgres.entity.ActivityExecutionEntity;
import com.leorces.persistence.postgres.entity.ProcessEntity;
import com.leorces.persistence.postgres.entity.ProcessExecutionEntity;
import com.leorces.persistence.postgres.utils.IdGenerator;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@AllArgsConstructor
public class ProcessMapper {

    private final DefinitionMapper definitionMapper;
    private final ActivityMapper activityMapper;
    private final VariableMapper variableMapper;

    public ProcessEntity toNewEntity(Process process) {
        var now = LocalDateTime.now();
        var businessKey = process.businessKey() != null ? process.businessKey() : IdGenerator.getNewId();
        return ProcessEntity.builder()
                .isNew(true)
                .id(process.id() == null ? IdGenerator.getNewId() : process.id())
                .rootProcessId(process.rootProcessId())
                .parentProcessId(process.parentId())
                .businessKey(businessKey)
                .processDefinitionId(process.definitionId())
                .processDefinitionKey(process.definitionKey())
                .state(ProcessState.ACTIVE.name())
                .createdAt(now)
                .updatedAt(now)
                .startedAt(now)
                .build();
    }

    public List<Process> toProcesses(List<ProcessEntity> entities) {
        return entities.stream()
                .map(this::toProcess)
                .toList();
    }

    public Process toProcess(ProcessEntity entity) {
        return Process.builder()
                .id(entity.getId())
                .rootProcessId(entity.getRootProcessId())
                .parentId(entity.getParentProcessId())
                .businessKey(entity.getBusinessKey())
                .state(ProcessState.valueOf(entity.getState()))
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .startedAt(entity.getStartedAt())
                .completedAt(entity.getCompletedAt())
                .definition(definitionMapper.toDefinition(entity))
                .variables(variableMapper.toVariables(entity.getVariablesJson()))
                .build();
    }

    public Process toProcess(ProcessExecutionEntity entity) {
        return Process.builder()
                .id(entity.getId())
                .rootProcessId(entity.getRootProcessId())
                .parentId(entity.getParentProcessId())
                .businessKey(entity.getBusinessKey())
                .state(ProcessState.valueOf(entity.getState()))
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .startedAt(entity.getStartedAt())
                .completedAt(entity.getCompletedAt())
                .definition(definitionMapper.toDefinition(entity))
                .variables(variableMapper.toVariables(entity.getVariablesJson()))
                .build();
    }

    public Process toProcess(ActivityExecutionEntity entity) {
        return Process.builder()
                .id(entity.getProcessId())
                .rootProcessId(entity.getRootProcessId())
                .parentId(entity.getProcessParentId())
                .businessKey(entity.getProcessBusinessKey())
                .state(ProcessState.valueOf(entity.getProcessState()))
                .createdAt(entity.getProcessCreatedAt())
                .updatedAt(entity.getProcessUpdatedAt())
                .startedAt(entity.getProcessStartedAt())
                .completedAt(entity.getProcessCompletedAt())
                .definition(definitionMapper.toDefinition(entity))
                .variables(variableMapper.toVariables(entity.getVariablesJson()))
                .build();
    }

    public ProcessExecution toExecution(ProcessExecutionEntity entity) {
        var process = toProcess(entity);
        var activities = activityMapper.toActivities(entity.getActivitiesJson(), process);
        return ProcessExecution.builder()
                .id(entity.getId())
                .rootProcessId(entity.getRootProcessId())
                .parentId(entity.getParentProcessId())
                .businessKey(entity.getBusinessKey())
                .state(ProcessState.valueOf(entity.getState()))
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .startedAt(entity.getStartedAt())
                .completedAt(entity.getCompletedAt())
                .definition(definitionMapper.toDefinition(entity))
                .variables(variableMapper.toVariables(entity.getVariablesJson()))
                .activities(activities)
                .build();
    }

}
