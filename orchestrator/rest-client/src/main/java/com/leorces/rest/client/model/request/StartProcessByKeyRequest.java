package com.leorces.rest.client.model.request;

import java.util.Map;

public record StartProcessByKeyRequest(
        String definitionKey,
        String businessKey,
        Map<String, Object> variables
) {

}