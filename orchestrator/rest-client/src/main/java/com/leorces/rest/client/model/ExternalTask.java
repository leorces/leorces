package com.leorces.rest.client.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.leorces.common.mapper.VariablesMapper;
import com.leorces.model.runtime.process.ProcessState;
import com.leorces.model.runtime.variable.Variable;
import com.leorces.rest.client.exception.VariableDeserializationException;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Builder(toBuilder = true)
public record ExternalTask(
        @JsonProperty("id") String id,
        @JsonProperty("processId") String processId,
        @JsonProperty("processBusinessKey") String businessKey,
        @JsonProperty("topic") String topicName,
        @JsonProperty("state") ProcessState state,
        @JsonProperty("retries") int retries,
        @JsonProperty("variables") List<Variable> variables,
        @JsonProperty("createdAt") LocalDateTime createdAt,
        @JsonProperty("updatedAt") LocalDateTime updatedAt,
        @JsonProperty("startedAt") LocalDateTime startedAt,
        @JsonProperty("completedAt") LocalDateTime completedAt,
        ObjectMapper objectMapper,
        VariablesMapper variablesMapper
) {

    @JsonCreator
    public static ExternalTask create(
            @JsonProperty("id") String id,
            @JsonProperty("processId") String processId,
            @JsonProperty("processBusinessKey") String businessKey,
            @JsonProperty("topic") String topicName,
            @JsonProperty("state") ProcessState state,
            @JsonProperty("retries") int retries,
            @JsonProperty("variables") List<Variable> variables,
            @JsonProperty("createdAt") LocalDateTime createdAt,
            @JsonProperty("updatedAt") LocalDateTime updatedAt,
            @JsonProperty("startedAt") LocalDateTime startedAt,
            @JsonProperty("completedAt") LocalDateTime completedAt) {
        return ExternalTask.builder()
                .id(id)
                .processId(processId)
                .businessKey(businessKey)
                .topicName(topicName)
                .state(state)
                .retries(retries)
                .variables(variables)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .startedAt(startedAt)
                .completedAt(completedAt)
                .build();
    }

    public <T> T getVariable(String name) {
        return (T) findVariableByName(name)
                .map(this::convertVariableToValue)
                .orElse(null);
    }

    public <T> T getVariable(String name, TypeReference<T> type) {
        return findVariableByName(name)
                .map(v -> deserializeWithType(v, type))
                .orElse(null);
    }

    public <T> T getVariable(String name, Class<T> clazz) {
        return findVariableByName(name)
                .map(variable -> convertVariableToCustomObject(variable, clazz))
                .orElse(null);
    }

    private Optional<Variable> findVariableByName(String name) {
        if (variables == null || variables.isEmpty()) {
            return Optional.empty();
        }
        return variables.stream()
                .filter(v -> Objects.equals(name, v.varKey()))
                .findFirst();
    }

    private Object convertVariableToValue(Variable variable) {
        return variablesMapper.convertStringToValue(variable.varValue(), variable.type());
    }

    private <T> T convertVariableToCustomObject(Variable variable, Class<T> clazz) {
        if (variable.varValue() == null) {
            return null;
        }

        try {
            return objectMapper.readValue(variable.varValue(), clazz);
        } catch (Exception e) {
            throw new VariableDeserializationException(String.format(
                    "Failed to deserialize variable '%s' to %s from value: %s",
                    variable.varKey(), clazz.getSimpleName(), variable.varValue()), e);
        }
    }

    private <T> T deserializeWithType(Variable variable, TypeReference<T> type) {
        if (variable.varValue() == null) {
            return null;
        }
        try {
            return objectMapper.readValue(variable.varValue(), type);
        } catch (Exception e) {
            throw new VariableDeserializationException("Failed to deserialize variable '%s'".formatted(variable.varKey()), e);
        }
    }

}
