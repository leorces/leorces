package com.leorces.rest.model.request;

import jakarta.validation.constraints.NotBlank;

import java.util.Map;

public record RunJobRequest(
        @NotBlank(message = "Job type cannot be null or blank")
        String type,
        Map<String, Object> input
) {
}
