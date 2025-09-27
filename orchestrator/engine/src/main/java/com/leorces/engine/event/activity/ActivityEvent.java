package com.leorces.engine.event.activity;

import com.leorces.engine.event.activity.cancel.CancelActivitiesByProcessIdEvent;
import com.leorces.engine.event.activity.cancel.CancelActivitiesEvent;
import com.leorces.engine.event.activity.complete.CompleteActivityAsync;
import com.leorces.engine.event.activity.complete.CompleteActivityByDefinitionIdEventAsync;
import com.leorces.engine.event.activity.complete.CompleteActivityByIdEventAsync;
import com.leorces.engine.event.activity.complete.CompleteActivitySuccessEvent;
import com.leorces.engine.event.activity.fail.FailActivityByIdEventAsync;
import com.leorces.engine.event.activity.fail.FailActivityEventAsync;
import com.leorces.engine.event.activity.fail.IncidentFailActivityEvent;
import com.leorces.engine.event.activity.retry.RetryActivitiesEventAsync;
import com.leorces.engine.event.activity.retry.RetryActivityByIdEventAsync;
import com.leorces.engine.event.activity.retry.RetryActivityEventAsync;
import com.leorces.engine.event.activity.run.RunActivitiesEventAsync;
import com.leorces.engine.event.activity.run.RunActivityByDefinitionEventAsync;
import com.leorces.engine.event.activity.run.RunActivityByDefinitionIdAsync;
import com.leorces.engine.event.activity.run.RunActivityEventAsync;
import com.leorces.engine.event.activity.terminate.TerminateActivitiesByProcessIdEvent;
import com.leorces.engine.event.activity.terminate.TerminateActivityByIdAsync;
import com.leorces.engine.event.activity.terminate.TerminateActivityEvent;
import com.leorces.engine.event.activity.trigger.TriggerActivityByDefinitionEventAsync;
import com.leorces.model.definition.activity.ActivityDefinition;
import com.leorces.model.runtime.activity.ActivityExecution;
import com.leorces.model.runtime.process.Process;
import org.springframework.context.ApplicationEvent;

import java.util.List;
import java.util.Map;

public class ActivityEvent extends ApplicationEvent {

    public ActivityEvent(Object source) {
        super(source);
    }

    public static RunActivityEventAsync runAsync(ActivityExecution activity) {
        return new RunActivityEventAsync(activity);
    }

    public static RunActivityByDefinitionEventAsync runByDefinitionAsync(ActivityDefinition definition, Process process) {
        return new RunActivityByDefinitionEventAsync(definition, process);
    }

    public static RunActivityByDefinitionIdAsync runByDefinitionIdAsync(String definitionId, String processId) {
        return new RunActivityByDefinitionIdAsync(definitionId, processId);
    }

    public static RunActivitiesEventAsync runAllAsync(List<ActivityDefinition> definitions, Process process) {
        return new RunActivitiesEventAsync(definitions, process);
    }

    public static CompleteActivityAsync completeAsync(ActivityExecution activity) {
        return new CompleteActivityAsync(activity);
    }

    public static CompleteActivityByIdEventAsync completeByIdAsync(String activityId) {
        return new CompleteActivityByIdEventAsync(activityId, Map.of());
    }

    public static CompleteActivityByIdEventAsync completeByIdAsync(String activityId, Map<String, Object> variables) {
        return new CompleteActivityByIdEventAsync(activityId, variables);
    }

    public static CompleteActivityByDefinitionIdEventAsync completeByDefinitionIdAsync(String definitionId, String processId) {
        return new CompleteActivityByDefinitionIdEventAsync(definitionId, processId);
    }

    public static CompleteActivitySuccessEvent completeSuccess(ActivityExecution activity) {
        return new CompleteActivitySuccessEvent(activity);
    }

    public static FailActivityEventAsync failAsync(ActivityExecution activity) {
        return new FailActivityEventAsync(activity);
    }

    public static FailActivityByIdEventAsync failByIdAsync(String activityId, Map<String, Object> variables) {
        return new FailActivityByIdEventAsync(activityId, variables);
    }

    public static IncidentFailActivityEvent incidentFailEvent(ActivityExecution activity) {
        return new IncidentFailActivityEvent(activity);
    }

    public static RetryActivityEventAsync retryAsync(ActivityExecution activity) {
        return new RetryActivityEventAsync(activity);
    }

    public static RetryActivityByIdEventAsync retryByIdAsync(String activityId) {
        return new RetryActivityByIdEventAsync(activityId);
    }

    public static RetryActivitiesEventAsync retryAllAsync(List<ActivityExecution> activityExecutions) {
        return new RetryActivitiesEventAsync(activityExecutions);
    }

    public static TriggerActivityByDefinitionEventAsync triggerByDefinitionAsync(ActivityDefinition definition, Process process) {
        return new TriggerActivityByDefinitionEventAsync(definition, process);
    }

    public static CancelActivitiesByProcessIdEvent cancelAllByProcessId(String processId) {
        return new CancelActivitiesByProcessIdEvent(processId);
    }

    public static CancelActivitiesEvent cancelAll(List<ActivityExecution> activities) {
        return new CancelActivitiesEvent(activities);
    }

    public static TerminateActivityEvent terminate(ActivityExecution activity) {
        return new TerminateActivityEvent(activity);
    }

    public static TerminateActivityByIdAsync terminateByIdAsync(String activityId) {
        return new TerminateActivityByIdAsync(activityId);
    }

    public static TerminateActivitiesByProcessIdEvent terminateAllByProcessId(String processId) {
        return new TerminateActivitiesByProcessIdEvent(processId);
    }

}
