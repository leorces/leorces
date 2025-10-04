package com.leorces.rest.model.request;

import jakarta.validation.constraints.NotBlank;

public record ProcessModificationRequest(
        @NotBlank(message = "Activity id cannot be null or blank")
        String activityId,
        @NotBlank(message = "Target definition id cannot be null or blank")
        String targetDefinitionId
) {
}
