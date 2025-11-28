package com.leorces.rest.client.model.request;

import lombok.Builder;

import java.util.Map;

@Builder
public record CorrelateMessageRequest(
        String message,
        String businessKey,
        Map<String, Object> correlationKeys,
        Map<String, Object> processVariables
) {

}
