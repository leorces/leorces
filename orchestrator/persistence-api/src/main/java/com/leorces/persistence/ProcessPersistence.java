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
     */
    void suspendByDefinitionId(String definitionId);

    /**
     * Suspends all process instances associated with the given
     * process definition key.
     * <p>
     * All process instances belonging to any version of the
     * specified process definition key are suspended.
     * </p>
     *
     * @param definitionKey the process definition key
     */
    void suspendByDefinitionKey(String definitionKey);

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
     */
    void resumeByDefinitionId(String definitionId);

    /**
     * Activates all suspended process instances associated with the given
     * process definition key.
     * <p>
     * All suspended process instances belonging to any version of the
     * specified process definition key are activated.
     * </p>
     *
     * @param definitionKey the process definition key
     */
    void resumeByDefinitionKey(String definitionKey);

    /**
     * Changes the state of a process by its identifier.
     *
     * @param processId the process identifier
     * @param state     the new process state
     */
    void changeState(String processId, ProcessState state);

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
     * Finds all fully completed processes with a limit.
     *
     * @param limit the maximum number of processes to retrieve
     * @return the list of fully completed processes
     */
    List<ProcessExecution> findAllFullyCompleted(int limit);

}
