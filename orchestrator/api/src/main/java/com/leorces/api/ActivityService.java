package com.leorces.api;

import com.leorces.model.runtime.activity.Activity;

import java.util.List;
import java.util.Map;

/**
 * Service for managing activity lifecycle operations.
 * Provides functionality to complete, fail, retry activities and poll for available activities.
 */
public interface ActivityService {

    /**
     * Runs an activity with the specified activity definition ID within a process.
     *
     * @param definitionId the unique identifier of the activity definition to run
     * @param processId    the unique identifier of the process instance
     */
    void run(String definitionId, String processId);

    /**
     * Completes the activity with the specified ID without variables.
     *
     * @param activityId the unique identifier of the activity to complete
     */
    void complete(String activityId);

    /**
     * Completes the activity with the specified ID and sets the provided variables.
     *
     * @param activityId the unique identifier of the activity to complete
     * @param variables  the variables to set during activity completion
     */
    void complete(String activityId, Map<String, Object> variables);

    /**
     * Fails the activity with the specified ID without variables.
     *
     * @param activityId the unique identifier of the activity to fail
     */
    void fail(String activityId);

    /**
     * Fails the activity with the specified ID and sets the provided variables.
     *
     * @param activityId the unique identifier of the activity to fail
     * @param variables  the variables to set during activity failure
     */
    void fail(String activityId, Map<String, Object> variables);

    /**
     * Terminates the activity with the specified ID.
     *
     * @param activityId the unique identifier of the activity to fail
     */
    void terminate(String activityId);

    /**
     * Retries the activity with the specified ID.
     *
     * @param activityId the unique identifier of the activity to retry
     */
    void retry(String activityId);

    /**
     * Polls for available activities based on topic and process definition key.
     *
     * @param topic                the topic to poll activities for
     * @param processDefinitionKey the key of the process definition
     * @param limit                the maximum number of activities to return
     * @return list of available activities matching the criteria
     */
    List<Activity> poll(String topic, String processDefinitionKey, int limit);

}
