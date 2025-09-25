package com.leorces.rest.model.request;

import jakarta.validation.constraints.NotBlank;

import java.util.Map;

public record StartProcessByIdRequest(
        @NotBlank(message = "Definition ID cannot be null or blank")
        String definitionId,
        String businessKey,
        Map<String, Object> variables
) {

}
