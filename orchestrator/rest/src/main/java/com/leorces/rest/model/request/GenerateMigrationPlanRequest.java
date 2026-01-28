package com.leorces.rest.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record GenerateMigrationPlanRequest(
        @NotBlank(message = "definitionKey must not be blank")
        String definitionKey,
        @NotNull(message = "fromVersion must not be null")
        @Positive(message = "fromVersion must be positive")
        Integer fromVersion,
        @NotNull(message = "toVersion must not be null")
        @Positive(message = "toVersion must be positive")
        Integer toVersion
) {
}
