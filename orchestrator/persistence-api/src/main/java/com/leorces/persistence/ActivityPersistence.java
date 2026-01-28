package com.leorces.persistence;

import com.leorces.model.runtime.activity.Activity;
import com.leorces.model.runtime.activity.ActivityExecution;
import com.leorces.model.runtime.activity.ActivityState;

import java.util.List;
import java.util.Optional;

/**
 * Persistence layer for managing activities throughout their lifecycle.
 * Provides operations for state transitions, querying, and polling activities.
 */
public interface ActivityPersistence {

    /**
     * Schedules an activity for future processing.
     *
     * @param activity the activity to schedule
     * @return the scheduled activity
     */
    ActivityExecution schedule(ActivityExecution activity);

    /**
     * Transitions an activity to an active state.
     *
     * @param activity the activity to run
     * @return the activity in active state
     */
    ActivityExecution run(ActivityExecution activity);

    /**
     * Completes an activity successfully.
     *
     * @param activity the activity to complete
     * @return the completed activity
     */
    ActivityExecution complete(ActivityExecution activity);

    /**
     * Terminates an activity forcefully.
     *
     * @param activity the activity to terminate
     * @return the terminated activity
     */
    ActivityExecution terminate(ActivityExecution activity);

    /**
     * Delete an activity.
     *
     * @param activity the activity to delete
     * @return the deleted activity
     */
    ActivityExecution delete(ActivityExecution activity);

    /**
     * Marks an activity as failed.
     *
     * @param activity the activity to mark as failed
     * @return the failed activity
     */
    ActivityExecution fail(ActivityExecution activity);

    /**
     * Changes the state of an activity by its identifier.
     *
     * @param activityId the activity identifier
     * @param state      the new activity state
     */
    void changeState(String activityId, ActivityState state);

    /**
     * Deletes all active activities by its identifier.
     *
     * @param processId     the process identifier
     * @param definitionIds the list of activity definition identifiers
     */
    void deleteAllActive(String processId, List<String> definitionIds);

    /**
     * Finds an activity by its unique identifier.
     *
     * @param id the unique identifier of the activity
     * @return an optional containing the activity if found, empty otherwise
     */
    Optional<ActivityExecution> findById(String id);

    /**
     * Finds an activity by process ID and definition ID.
     *
     * @param processId    the process identifier
     * @param definitionId the activity definition identifier
     * @return an optional containing the activity if found, empty otherwise
     */
    Optional<ActivityExecution> findByDefinitionId(String processId, String definitionId);

    /**
     * Finds all active activities for specified definition IDs within a process.
     *
     * @param processId     the process identifier
     * @param definitionIds the list of activity definition identifiers
     * @return the list of active activities
     */
    List<ActivityExecution> findActive(String processId, List<String> definitionIds);

    /**
     * Finds all active activities within a process.
     *
     * @param processId the process identifier
     * @return the list of active activities
     */
    List<ActivityExecution> findActive(String processId);

    /**
     * Finds all failed activities within a process.
     *
     * @param processId the process identifier
     * @return the list of failed activities
     */
    List<ActivityExecution> findFailed(String processId);

    /**
     * Finds all timed out activities.
     *
     * @param limit the maximum number of activities to retrieve
     * @return the list of timed-out activities
     */
    List<ActivityExecution> findTimedOut(int limit);

    /**
     * Polls for available activities to execute from a specific topic.
     *
     * @param topic                the topic name to poll from
     * @param processDefinitionKey the process definition key
     * @param limit                the maximum number of activities to retrieve
     * @return the list of available activities for execution
     */
    List<Activity> poll(String topic, String processDefinitionKey, int limit);

    /**
     * Checks if any activity within a process has failed.
     *
     * @param processId the process identifier
     * @return true if any activity has failed, false otherwise
     */
    boolean isAnyFailed(String processId);

    /**
     * Checks if all activities within a process are completed.
     *
     * @param processId the process identifier
     * @return true if all activities are completed, false otherwise
     */
    boolean isAllCompleted(String processId);

    /**
     * Checks if all activities for specified definition IDs are completed.
     *
     * @param processId     the process identifier
     * @param definitionIds the list of activity definition identifiers
     * @return true if all specified activities are completed, false otherwise
     */
    boolean isAllCompleted(String processId, List<String> definitionIds);

}
