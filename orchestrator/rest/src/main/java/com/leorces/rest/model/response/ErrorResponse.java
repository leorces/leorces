package com.leorces.rest.model.response;

import java.time.LocalDateTime;
import java.util.Map;

public record ErrorResponse(
        String error,
        String message,
        int status,
        LocalDateTime timestamp,
        Map<String, String> validationErrors
) {

}
