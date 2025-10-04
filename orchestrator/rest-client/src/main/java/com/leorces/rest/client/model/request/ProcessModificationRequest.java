package com.leorces.rest.client.model.request;

public record ProcessModificationRequest(
        String activityId,
        String targetDefinitionId
) {
}
