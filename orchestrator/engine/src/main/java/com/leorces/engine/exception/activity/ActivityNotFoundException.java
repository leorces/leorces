package com.leorces.engine.exception.activity;

import com.leorces.model.runtime.activity.ActivityExecution;

public class ActivityNotFoundException extends RuntimeException {

    private ActivityNotFoundException(String message) {
        super(message);
    }

    public static ActivityNotFoundException activityDefinitionNotFound(String definitionId, String processId) {
        return new ActivityNotFoundException(
                "Activity definition not found for definitionId: %s in process: %s".formatted(definitionId, processId)
        );
    }

    public static ActivityNotFoundException activityNotFoundById(String id) {
        return new ActivityNotFoundException(
                "Activity not found for id: %s".formatted(id)
        );
    }

    public static ActivityNotFoundException activityNotFoundByProcessAndDefinition(String processId, String definitionId) {
        return new ActivityNotFoundException(
                "Activity not found for process: %s and definition: %s".formatted(processId, definitionId)
        );
    }

    public static ActivityNotFoundException eventSubprocessNotFound() {
        return new ActivityNotFoundException(
                "No event subprocess found"
        );
    }

    public static ActivityNotFoundException startEventNotFoundForSubprocess(String subprocessId) {
        return new ActivityNotFoundException(
                "Start event not found for subprocess: %s".formatted(subprocessId)
        );
    }

    public static ActivityNotFoundException startActivityNotFoundInProcess(String processId) {
        return new ActivityNotFoundException(
                "Start activity not found in process definition for process: %s".formatted(processId)
        );
    }

    public static ActivityNotFoundException parentActivityDefinitionNotFound(ActivityExecution activity) {
        return new ActivityNotFoundException(
                "Parent activity definition not found for definitionId: %s in process: %s".formatted(activity.definitionId(), activity.processId())
        );
    }

}
