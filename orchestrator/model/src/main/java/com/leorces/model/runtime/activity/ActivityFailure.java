package com.leorces.model.runtime.activity;

import org.apache.commons.lang3.exception.ExceptionUtils;

public record ActivityFailure(
        String reason,
        String trace
) {
    public static ActivityFailure of(Exception exception) {
        return new ActivityFailure(exception.getMessage(), ExceptionUtils.getStackTrace(exception));
    }

    public static ActivityFailure of(String reason) {
        return new ActivityFailure(reason, null);
    }

}
