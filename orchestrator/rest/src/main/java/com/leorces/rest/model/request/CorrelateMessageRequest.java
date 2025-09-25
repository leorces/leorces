package com.leorces.rest.model.request;

import jakarta.validation.constraints.NotBlank;

import java.util.Map;

public record CorrelateMessageRequest(
        @NotBlank(message = "Message cannot be null or blank")
        String message,
        String businessKey,
        Map<String, Object> correlationKeys,
        Map<String, Object> processVariables
) {

}
