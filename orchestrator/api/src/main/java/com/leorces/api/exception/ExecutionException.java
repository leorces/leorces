package com.leorces.api.exception;

import com.leorces.model.runtime.activity.ActivityExecution;
import com.leorces.model.runtime.process.Process;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

/**
 * Exception thrown when command execution fails.
 */
@Getter
public class ExecutionException extends RuntimeException {

    private final String detailedMessage;
    private final Map<String, Object> details;

    private ExecutionException(String message,
                               String detailedMessage,
                               Map<String, Object> details,
                               Throwable cause) {
        super(message, cause);
        this.detailedMessage = detailedMessage;
        this.details = details;
    }

    public static ExecutionException of(String message) {
        return new ExecutionException(message, null, null, null);
    }

    public static ExecutionException of(String message, Throwable cause) {
        return new ExecutionException(message, null, null, cause);
    }

    public static ExecutionException of(String message, ActivityExecution activity) {
        return new ExecutionException(
                message,
                message,
                activityDetails(activity),
                null
        );
    }

    public static ExecutionException of(String message,
                                        ActivityExecution activity,
                                        Throwable cause) {
        return new ExecutionException(
                message,
                message,
                activityDetails(activity),
                cause
        );
    }

    public static ExecutionException of(String message,
                                        String detailedMessage,
                                        ActivityExecution activity) {
        return new ExecutionException(
                message,
                detailedMessage,
                activityDetails(activity),
                null
        );
    }

    public static ExecutionException of(String message,
                                        Process process) {
        return new ExecutionException(
                message,
                message,
                processDetails(process),
                null
        );
    }

    public static ExecutionException of(String message,
                                        String detailedMessage,
                                        Process process) {
        return new ExecutionException(
                message,
                detailedMessage,
                processDetails(process),
                null
        );
    }

    public static ExecutionException of(String message,
                                        String detailedMessage) {
        return new ExecutionException(
                message,
                detailedMessage,
                Map.of(),
                null
        );
    }

    private static Map<String, Object> activityDetails(ActivityExecution activity) {
        if (activity == null) {
            return Map.of();
        }

        var details = new HashMap<String, Object>();
        details.put("activityId", activity.id());
        details.put("activityType", activity.type());
        details.put("activityDefinitionId", activity.definitionId());
        details.putAll(processDetails(activity.process()));
        return details;
    }

    private static Map<String, Object> processDetails(Process process) {
        if (process == null) {
            return Map.of();
        }
        var details = new HashMap<String, Object>();
        details.put("processId", process.id());
        details.put("processDefinitionId", process.definitionId());
        details.put("processDefinitionKey", process.definitionKey());
        details.put("processBusinessKey", process.businessKey());
        details.put("processState", process.state());
        details.put("suspended", process.suspended());
        return details;
    }

}
