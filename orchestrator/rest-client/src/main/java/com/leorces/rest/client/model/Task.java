package com.leorces.rest.client.model;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
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
public record Task(
        @JsonProperty("id") String id,
        @JsonProperty("businessKey") String businessKey,
        @JsonProperty("definitionId") String definitionId,
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
    public static Task create(
            @JsonProperty("id") String id,
            @JsonProperty("businessKey") String businessKey,
            @JsonProperty("definitionId") String definitionId,
            @JsonProperty("state") ProcessState state,
            @JsonProperty("retries") int retries,
            @JsonProperty("variables") List<Variable> variables,
            @JsonProperty("createdAt") LocalDateTime createdAt,
            @JsonProperty("updatedAt") LocalDateTime updatedAt,
            @JsonProperty("startedAt") LocalDateTime startedAt,
            @JsonProperty("completedAt") LocalDateTime completedAt) {
        return Task.builder()
                .id(id)
                .businessKey(businessKey)
                .definitionId(definitionId)
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


    public <T> T getVariable(Class<T> clazz, String name) {
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
}
