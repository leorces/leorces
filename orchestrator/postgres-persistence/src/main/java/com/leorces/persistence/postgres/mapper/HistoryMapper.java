package com.leorces.persistence.postgres.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.leorces.model.runtime.process.ProcessExecution;
import com.leorces.persistence.postgres.entity.HistoryEntity;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.zip.GZIPOutputStream;

@Slf4j
@Component
@AllArgsConstructor
public class HistoryMapper {

    private final ObjectMapper objectMapper;

    public List<HistoryEntity> toEntities(List<ProcessExecution> executions) {
        return executions.stream()
                .map(this::toEntity)
                .toList();
    }

    public HistoryEntity toEntity(ProcessExecution process) {
        return HistoryEntity.builder()
                .processId(process.id())
                .rootProcessId(process.rootProcessId())
                .parentProcessId(process.parentId())
                .businessKey(process.businessKey())
                .data(toGzip(process))
                .createdAt(process.createdAt())
                .updatedAt(process.updatedAt())
                .startedAt(process.startedAt())
                .completedAt(process.completedAt())
                .isNew(true)
                .build();
    }

    public List<ProcessExecution> toExecutions(List<HistoryEntity> entities) {
        return entities.stream()
                .map(this::toExecution)
                .toList();
    }

    public ProcessExecution toExecution(HistoryEntity entity) {
        return fromGzip(entity.getData());
    }

    private byte[] toGzip(ProcessExecution execution) {
        try {
            var json = objectMapper.writeValueAsString(execution);
            var baos = new ByteArrayOutputStream();
            try (var gzip = new GZIPOutputStream(baos)) {
                gzip.write(json.getBytes(StandardCharsets.UTF_8));
            }
            return baos.toByteArray();
        } catch (Exception e) {
            log.error("Failed to serialize process data to json", e);
            return null;
        }
    }

    private ProcessExecution fromGzip(byte[] compressedData) {
        if (compressedData == null || compressedData.length == 0) {
            return null;
        }

        try (var bais = new java.io.ByteArrayInputStream(compressedData);
             var gzip = new java.util.zip.GZIPInputStream(bais)) {
            var jsonBytes = gzip.readAllBytes();
            return objectMapper.readValue(jsonBytes, ProcessExecution.class);
        } catch (Exception e) {
            log.error("Failed to deserialize process data from gzip", e);
            return null;
        }
    }

}
