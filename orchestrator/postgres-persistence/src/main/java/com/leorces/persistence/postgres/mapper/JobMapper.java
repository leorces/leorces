package com.leorces.persistence.postgres.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.leorces.model.job.Job;
import com.leorces.model.job.JobState;
import com.leorces.persistence.postgres.entity.JobEntity;
import com.leorces.persistence.postgres.exception.JobMetadataSerializationException;
import com.leorces.persistence.postgres.utils.IdGenerator;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.postgresql.util.PGobject;
import org.springframework.stereotype.Component;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@AllArgsConstructor
public class JobMapper {

    private final ObjectMapper objectMapper;

    public JobEntity toEntity(Job job) {
        return JobEntity.builder()
                .isNew(job.id() == null)
                .id(job.id() == null ? IdGenerator.getNewId() : job.id())
                .type(job.type())
                .state(job.state().toString())
                .input(paramsToJson(job.input()))
                .output(paramsToJson(job.output()))
                .failureReason(job.failureReason())
                .failureTrace(job.failureTrace())
                .retries(job.retries())
                .createdAt(job.createdAt())
                .updatedAt(job.updatedAt())
                .startedAt(job.startedAt())
                .completedAt(job.completedAt())
                .build();
    }

    public List<Job> toJobs(List<JobEntity> entities) {
        return entities.stream()
                .map(this::toJob)
                .toList();
    }

    public Job toJob(JobEntity entity) {
        return Job.builder()
                .id(entity.getId())
                .type(entity.getType())
                .state(JobState.valueOf(entity.getState()))
                .input(paramsFromJson(entity.getInput()))
                .output(paramsFromJson(entity.getOutput()))
                .failureReason(entity.getFailureReason())
                .failureTrace(entity.getFailureTrace())
                .retries(entity.getRetries())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .startedAt(entity.getStartedAt())
                .completedAt(entity.getCompletedAt())
                .build();
    }

    private PGobject paramsToJson(Map<String, Object> params) {
        if (params == null || params.isEmpty()) {
            return null;
        }

        try {
            var json = objectMapper.writeValueAsString(params);
            var object = new PGobject();
            object.setType("jsonb");
            object.setValue(json);
            return object;
        } catch (JsonProcessingException | SQLException e) {
            throw new JobMetadataSerializationException("Failed to serialize params: %s".formatted(params), e);
        }
    }

    private Map<String, Object> paramsFromJson(PGobject paramsJson) {
        if (paramsJson == null || paramsJson.getValue() == null) {
            return Map.of();
        }

        try {
            return objectMapper.readValue(
                    paramsJson.getValue(),
                    Map.class
            );
        } catch (JsonProcessingException e) {
            log.error("Failed to deserialize params from json: {}", paramsJson.getValue(), e);
            throw new JobMetadataSerializationException(
                    "Failed to deserialize params: %s".formatted(paramsJson.getValue()),
                    e
            );
        }
    }

}
