package com.leorces.engine.exception.activity;

import com.leorces.model.runtime.activity.ActivityExecution;

public class GatewayException extends RuntimeException {

    public GatewayException(String message) {
        super(message);
    }

    public static GatewayException noValidPath(ActivityExecution activity) {
        return new GatewayException(
                "No valid path for gateway with id: %s, definitionId: %s and processId: %s"
                        .formatted(activity.id(), activity.definitionId(), activity.processId())
        );
    }

}
