package com.leorces.rest.model.request;

import jakarta.validation.constraints.NotBlank;

import java.util.Map;

public record StartProcessByKeyRequest(
        @NotBlank(message = "Definition key cannot be null or blank")
        String definitionKey,
        String businessKey,
        Map<String, Object> variables
) {

}