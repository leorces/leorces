package com.leorces.rest.client.model.request;

import java.util.Map;

public record RunJobRequest(
        String type,
        Map<String, Object> input
) {
}
