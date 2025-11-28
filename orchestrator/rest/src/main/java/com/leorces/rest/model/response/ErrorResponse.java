package com.leorces.rest.model.response;

import lombok.Builder;

import java.time.LocalDateTime;
import java.util.Map;

@Builder
public record ErrorResponse(
        String error,
        String message,
        String detailedMessage,
        int status,
        LocalDateTime timestamp,
        Map<String, String> validationErrors
) {

}
