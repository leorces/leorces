package com.leorces.persistence;

import com.leorces.model.pagination.Pageable;
import com.leorces.model.pagination.PageableData;
import com.leorces.model.runtime.process.Process;
import com.leorces.model.runtime.process.ProcessExecution;
import com.leorces.model.runtime.process.ProcessState;
import com.leorces.model.search.ProcessFilter;

import java.util.List;
import java.util.Optional;

/**
 * Persistence layer for managing processes throughout their lifecycle.
 * Provides operations for state transitions, querying, and managing process instances.
 */
public interface ProcessPersistence {

    /**
     * Transitions a process to running state.
     *
     * @param process the process to run
     * @return the process in running state
     */
    Process run(Process process);

    /**
     * Completes a process successfully.
     *
     * @param processId the process identifier
     */
    void complete(String processId);

    /**
     * Terminates a process forcefully.
     *
     * @param processId the process identifier
     */
    void terminate(String processId);

    /**
     * Deletes a process.
     *
     * @param processId the process identifier
     */
    void delete(String processId);

    /**
     * Marks a process as having an incident.
     *
     * @param processId the process identifier
     */
    void incident(String processId);

    /**
     * Suspends a process instance by its identifier.
     * <p>
     * A suspended process remains persisted but is prevented from
     * further execution until it is activated again.
     * </p>
     *
     * @param processId the process identifier
     */
    void suspendById(String processId);

    /**
     * Suspends all process instances associated with the given
     * process definition identifier.
     * <p>
     * This operation updates the state of all matching process
     * instances to suspended.
     * </p>
     *
     * @param definitionId the process definition identifier
     * @param batchSize    the maximum number of processes to suspend in a single batch
     * @return the number of suspended process instances
     */
    int suspendByDefinitionId(String definitionId, int batchSize);

    /**
     * Suspends all process instances associated with the given
     * process definition key.
     * <p>
     * All process instances belonging to any version of the
     * specified process definition key are suspended.
     * </p>
     *
     * @param definitionKey the process definition key
     * @param batchSize     the maximum number of processes to suspend in a single batch
     * @return the number of suspended process instances
     */
    int suspendByDefinitionKey(String definitionKey, int batchSize);

    /**
     * Activates a suspended process instance by its identifier.
     * <p>
     * Activation restores the process to an executable state
     * and allows further state transitions.
     * </p>
     *
     * @param processId the process identifier
     */
    void resumeById(String processId);

    /**
     * Activates all suspended process instances associated with the given
     * process definition identifier.
     * <p>
     * This operation updates the state of all matching suspended
     * process instances to active.
     * </p>
     *
     * @param definitionId the process definition identifier
     * @param batchSize    the maximum number of processes to activate in a single batch
     * @return the number of activated process instances
     */
    int resumeByDefinitionId(String definitionId, int batchSize);

    /**
     * Activates all suspended process instances associated with the given
     * process definition key.
     * <p>
     * All suspended process instances belonging to any version of the
     * specified process definition key are activated.
     * </p>
     *
     * @param definitionKey the process definition key
     * @param batchSize     the maximum number of processes to activate in a single batch
     * @return the number of activated process instances
     */
    int resumeByDefinitionKey(String definitionKey, int batchSize);

    /**
     * Changes the state of a process by its identifier.
     *
     * @param processId the process identifier
     * @param state     the new process state
     */
    void changeState(String processId, ProcessState state);

    /**
     * Updates the process definition ID for all processes with the given definition ID.
     *
     * @param fromDefinitionId the current process definition ID
     * @param toDefinitionId   the new process definition ID
     * @param batchSize        the maximum number of processes to update in a single batch
     * @return the number of updated process instances
     */
    int updateDefinitionId(String fromDefinitionId, String toDefinitionId, int batchSize);

    /**
     * Updates the process definition identifier for all processes with the given definition identifier.
     *
     * @param toDefinitionId the new process definition identifier
     * @param processIds     the list of process identifiers to update
     * @return the number of updated process instances
     */
    int updateDefinitionId(String toDefinitionId, List<String> processIds);

    /**
     * Finds a process by its unique identifier.
     *
     * @param processId the process identifier
     * @return an optional containing the process if found, empty otherwise
     */
    Optional<Process> findById(String processId);

    /**
     * Finds a process execution by its unique identifier.
     *
     * @param processId the process identifier
     * @return an optional containing the process execution if found, empty otherwise
     */
    Optional<ProcessExecution> findExecutionById(String processId);

    /**
     * Finds process executions by its definition ID for update operations.
     *
     * @param definitionId the process definition identifier
     * @param limit        the maximum number of executions to retrieve
     * @return list of the process executions if found, empty otherwise
     */
    List<ProcessExecution> findExecutionsForUpdate(String definitionId, int limit);

    /**
     * Finds all processes.
     *
     * @param filter contains process data to search for
     * @return the list of processes
     */
    List<Process> findAll(ProcessFilter filter);

    /**
     * Finds all processes with pagination support.
     *
     * @param pageable the pagination parameters
     * @return pageable data containing processes and total count
     */
    PageableData<Process> findAll(Pageable pageable);

    /**
     * Finds all fully completed processes with a limit for update operations.
     *
     * @param limit the maximum number of processes to retrieve
     * @return the list of fully completed processes
     */
    List<ProcessExecution> findAllFullyCompletedForUpdate(int limit);

}
