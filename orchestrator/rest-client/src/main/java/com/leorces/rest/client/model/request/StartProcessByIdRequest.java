package com.leorces.rest.client.model.request;


import java.util.Map;


public record StartProcessByIdRequest(
        String definitionId,
        String businessKey,
        Map<String, Object> variables
) {

}
