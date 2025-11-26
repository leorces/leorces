package com.leorces.model.search;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Builder;

import java.util.Map;

@Builder
public record ProcessFilter(
        String processDefinitionKey,
        String processDefinitionId,
        String businessKey,
        Map<String, Object> variables
) {

    @JsonIgnore
    public boolean isEmpty() {
        return (processDefinitionKey == null || processDefinitionKey.isBlank()) &&
                (processDefinitionId == null || processDefinitionId.isBlank()) &&
                (businessKey == null || businessKey.isBlank()) &&
                (variables == null || variables.isEmpty());
    }

}
