package com.leorces.persistence.postgres.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.leorces.model.definition.ErrorItem;
import com.leorces.model.definition.ProcessDefinition;
import com.leorces.model.definition.ProcessDefinitionMetadata;
import com.leorces.model.definition.activity.ActivityDefinition;
import com.leorces.model.definition.activity.ActivityDefinitionDeserializer;
import com.leorces.persistence.postgres.entity.ActivityExecutionEntity;
import com.leorces.persistence.postgres.entity.ProcessDefinitionEntity;
import com.leorces.persistence.postgres.entity.ProcessEntity;
import com.leorces.persistence.postgres.entity.ProcessExecutionEntity;
import com.leorces.persistence.postgres.exception.DefinitionDeserializationException;
import com.leorces.persistence.postgres.exception.DefinitionSerializationException;
import com.leorces.persistence.postgres.utils.IdGenerator;
import lombok.AllArgsConstructor;
import org.postgresql.util.PGobject;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

@Component
@AllArgsConstructor
public class DefinitionMapper {

    private final ObjectMapper objectMapper;

    public ProcessDefinitionEntity toNewEntity(ProcessDefinition processDefinition, int version) {
        return ProcessDefinitionEntity.builder()
                .isNew(true)
                .id(IdGenerator.getNewId())
                .key(processDefinition.key())
                .name(processDefinition.name())
                .version(version)
                .schema(processDefinition.metadata().schema())
                .origin(processDefinition.metadata().origin())
                .deployment(processDefinition.metadata().deployment())
                .createdAt(processDefinition.createdAt() == null ? LocalDateTime.now() : processDefinition.createdAt())
                .updatedAt(LocalDateTime.now())
                .data(toJson(processDefinition))
                .build();
    }

    public List<ProcessDefinition> toDefinitions(List<ProcessDefinitionEntity> entities) {
        return entities.stream()
                .map(this::toDefinition)
                .toList();
    }

    public ProcessDefinition toDefinition(ProcessDefinitionEntity entity) {
        var data = fromJson(entity.getData());
        return ProcessDefinition.builder()
                .id(entity.getId())
                .key(entity.getKey())
                .name(entity.getName())
                .version(entity.getVersion())
                .activities(data.activities())
                .messages(data.messages())
                .errors(data.errors())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .suspended(entity.isSuspended())
                .metadata(ProcessDefinitionMetadata.builder()
                        .schema(entity.getSchema())
                        .origin(entity.getOrigin())
                        .deployment(entity.getDeployment())
                        .build())
                .build();
    }

    public ProcessDefinition toDefinition(ProcessEntity entity) {
        if (entity.getDefinitionData() == null) {
            return null;
        }

        var data = fromJson(entity.getDefinitionData());
        return ProcessDefinition.builder()
                .id(entity.getProcessDefinitionId())
                .key(entity.getProcessDefinitionKey())
                .name(entity.getDefinitionName())
                .version(entity.getDefinitionVersion())
                .suspended(entity.isSuspended())
                .activities(data.activities())
                .messages(data.messages())
                .errors(data.errors())
                .createdAt(entity.getDefinitionCreatedAt())
                .updatedAt(entity.getDefinitionUpdatedAt())
                .metadata(ProcessDefinitionMetadata.builder()
                        .schema(entity.getDefinitionSchema())
                        .origin(entity.getDefinitionOrigin())
                        .deployment(entity.getDefinitionDeployment())
                        .build())
                .build();
    }

    public ProcessDefinition toDefinition(ProcessExecutionEntity entity) {
        var data = fromJson(entity.getDefinitionData());
        return ProcessDefinition.builder()
                .id(entity.getProcessDefinitionId())
                .key(entity.getProcessDefinitionKey())
                .name(entity.getDefinitionName())
                .version(entity.getDefinitionVersion())
                .suspended(entity.isSuspended())
                .activities(data.activities())
                .messages(data.messages())
                .errors(data.errors())
                .createdAt(entity.getDefinitionCreatedAt())
                .updatedAt(entity.getDefinitionUpdatedAt())
                .metadata(ProcessDefinitionMetadata.builder()
                        .schema(entity.getDefinitionSchema())
                        .origin(entity.getDefinitionOrigin())
                        .deployment(entity.getDefinitionDeployment())
                        .build())
                .build();
    }

    public ProcessDefinition toDefinition(ActivityExecutionEntity entity) {
        var data = fromJson(entity.getDefinitionData());
        return ProcessDefinition.builder()
                .id(entity.getDefinitionId())
                .key(entity.getProcessDefinitionKey())
                .name(entity.getDefinitionName())
                .version(entity.getDefinitionVersion())
                .suspended(entity.isDefinitionSuspended())
                .activities(data.activities())
                .messages(data.messages())
                .errors(data.errors())
                .createdAt(entity.getDefinitionCreatedAt())
                .updatedAt(entity.getDefinitionUpdatedAt())
                .metadata(ProcessDefinitionMetadata.builder()
                        .schema(entity.getDefinitionSchema())
                        .origin(entity.getDefinitionOrigin())
                        .deployment(entity.getDefinitionDeployment())
                        .build())
                .build();
    }

    public PGobject toJson(ProcessDefinition processDefinition) {
        var data = new ProcessDefinitionData(
                processDefinition.activities(),
                processDefinition.messages(),
                processDefinition.errors()
        );
        try {
            var json = objectMapper.writeValueAsString(data);
            var object = new PGobject();
            object.setType("jsonb");
            object.setValue(json);
            return object;
        } catch (JsonProcessingException | SQLException e) {
            throw new DefinitionSerializationException("Failed to serialize ProcessDefinitionData with %d activities and %d messages".formatted(data.activities().size(), data.messages().size()), e);
        }
    }

    private ProcessDefinitionData fromJson(PGobject jsonData) {
        if (jsonData == null) {
            return null;
        }
        try {
            return objectMapper.readValue(jsonData.getValue(), ProcessDefinitionData.class);
        } catch (IOException e) {
            throw new DefinitionDeserializationException("Failed to deserialize ProcessDefinitionData from JSON string", e);
        }
    }

    private record ProcessDefinitionData(
            @JsonDeserialize(contentUsing = ActivityDefinitionDeserializer.class)
            List<ActivityDefinition> activities,
            List<String> messages,
            List<ErrorItem> errors
    ) {

    }

}
