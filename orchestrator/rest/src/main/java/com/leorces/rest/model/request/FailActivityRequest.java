package com.leorces.rest.model.request;

import com.leorces.model.runtime.activity.ActivityFailure;

import java.util.Map;

public record FailActivityRequest(
        ActivityFailure failure,
        Map<String, Object> variables
) {
}
