package com.leorces.persistence.postgres.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.leorces.model.runtime.activity.ActivityExecution;
import com.leorces.model.runtime.process.Process;
import com.leorces.model.runtime.variable.Variable;
import com.leorces.persistence.postgres.entity.VariableEntity;
import com.leorces.persistence.postgres.utils.IdGenerator;
import lombok.AllArgsConstructor;
import org.postgresql.util.PGobject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Component
@AllArgsConstructor
public class VariableMapper {

    private final ObjectMapper objectMapper;

    public List<VariableEntity> toEntities(Process process) {
        var variables = process.variables().stream()
                .filter(variable -> variable.id() == null)
                .toList();

        if (variables.isEmpty()) {
            return Collections.emptyList();
        }

        return process.variables().stream()
                .map(variable -> toEntity(process, variable))
                .toList();
    }

    public List<VariableEntity> toEntities(ActivityExecution activity) {
        var variables = activity.variables().stream()
                .filter(variable -> variable.id() == null)
                .toList();

        if (variables.isEmpty()) {
            return Collections.emptyList();
        }

        return activity.variables().stream()
                .map(variable -> toEntity(activity, variable))
                .toList();
    }

    public List<VariableEntity> toEntities(List<Variable> variables) {
        if (variables == null || variables.isEmpty()) {
            return Collections.emptyList();
        }

        return variables.stream()
                .map(this::toEntity)
                .toList();
    }

    public List<VariableEntity> toEntities(PGobject json) {
        if (json == null || json.getValue() == null) {
            return Collections.emptyList();
        }
        try {
            return objectMapper.readValue(
                    json.getValue(),
                    new TypeReference<List<VariableEntity>>() {
                    }
            );
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to deserialize variables JSON", e);
        }
    }

    public VariableEntity toEntity(Process process, Variable variable) {
        var now = LocalDateTime.now();
        return VariableEntity.builder()
                .isNew(variable.id() == null)
                .id(variable.id() == null ? IdGenerator.getNewId() : variable.id())
                .processId(process.id())
                .executionId(process.id())
                .executionDefinitionId(process.definitionId())
                .varKey(variable.varKey())
                .varValue(variable.varValue())
                .type(variable.type())
                .createdAt(variable.createdAt() == null ? now : variable.createdAt())
                .updatedAt(now)
                .build();
    }

    public VariableEntity toEntity(ActivityExecution activity, Variable variable) {
        var now = LocalDateTime.now();
        return VariableEntity.builder()
                .isNew(variable.id() == null)
                .id(variable.id() == null ? IdGenerator.getNewId() : variable.id())
                .processId(activity.processId())
                .executionId(activity.id())
                .executionDefinitionId(activity.definitionId())
                .varKey(variable.varKey())
                .varValue(variable.varValue())
                .type(variable.type())
                .createdAt(variable.createdAt() == null ? now : variable.createdAt())
                .updatedAt(now)
                .build();
    }

    public VariableEntity toEntity(Variable variable) {
        var now = LocalDateTime.now();
        return VariableEntity.builder()
                .isNew(variable.id() == null)
                .id(variable.id() == null ? IdGenerator.getNewId() : variable.id())
                .processId(variable.processId())
                .executionId(variable.executionId())
                .executionDefinitionId(variable.executionDefinitionId())
                .varKey(variable.varKey())
                .varValue(variable.varValue())
                .type(variable.type())
                .createdAt(variable.createdAt() == null ? now : variable.createdAt())
                .updatedAt(now)
                .build();
    }

    public List<Variable> toVariables(PGobject json) {
        var entities = toEntities(json);
        return toVariables(entities);
    }

    public List<Variable> toVariables(List<VariableEntity> entities) {
        return entities.stream()
                .map(this::toVariable)
                .toList();
    }

    public Variable toVariable(VariableEntity entity) {
        return Variable.builder()
                .id(entity.getId())
                .processId(entity.getProcessId())
                .executionId(entity.getExecutionId())
                .executionDefinitionId(entity.getExecutionDefinitionId())
                .varKey(entity.getVarKey())
                .varValue(entity.getVarValue())
                .type(entity.getType())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public List<Variable> toVariables(Object variablesJson) {
        switch (variablesJson) {
            case null -> {
                return Collections.emptyList();
            }
            case PGobject pgobject -> {
                return toVariables(pgobject);
            }


            // Handle the case where variablesJson is a List (from JSON deserialization)
            case List ignored -> {
                try {
                    var jsonString = objectMapper.writeValueAsString(variablesJson);
                    var pgobject = new PGobject();
                    pgobject.setType("json");
                    pgobject.setValue(jsonString);
                    return toVariables(pgobject);
                } catch (Exception e) {
                    throw new RuntimeException("Failed to convert variables to PGobject", e);
                }
            }
            default -> {
            }
        }

        return Collections.emptyList();
    }

}
