package com.leorces.rest.client.model.request;


import java.util.Map;


public record CorrelateMessageRequest(
        String message,
        String businessKey,
        Map<String, Object> correlationKeys,
        Map<String, Object> processVariables
) {

}
