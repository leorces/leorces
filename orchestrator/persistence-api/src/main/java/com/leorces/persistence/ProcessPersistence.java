package com.leorces.persistence;


import com.leorces.model.pagination.Pageable;
import com.leorces.model.pagination.PageableData;
import com.leorces.model.runtime.process.Process;
import com.leorces.model.runtime.process.ProcessExecution;
import com.leorces.model.runtime.process.ProcessState;

import java.util.List;
import java.util.Map;
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
     * @param process the process to complete
     * @return the completed process
     */
    Process complete(Process process);

    /**
     * Cancels a process.
     *
     * @param process the process to cancel
     * @return the cancelled process
     */
    Process cancel(Process process);

    /**
     * Terminates a process forcefully.
     *
     * @param process the process to terminate
     * @return the terminated process
     */
    Process terminate(Process process);

    /**
     * Marks a process as having an incident.
     *
     * @param process the process to mark with incident
     * @return the process with incident state
     */
    Process incident(Process process);

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
     * Finds all processes by their business key.
     *
     * @param businessKey the business key to search for
     * @return the list of processes matching the business key
     */
    List<Process> findByBusinessKey(String businessKey);

    /**
     * Finds all processes by their variables.
     *
     * @param variables the map of variables to search for
     * @return the list of processes matching the variables
     */
    List<Process> findByVariables(Map<String, Object> variables);

    /**
     * Finds all processes by both business key and variables.
     *
     * @param businessKey the business key to search for
     * @param variables   the map of variables to search for
     * @return the list of processes matching both business key and variables
     */
    List<Process> findByBusinessKeyAndVariables(String businessKey, Map<String, Object> variables);

    /**
     * Finds all fully completed processes with a limit.
     *
     * @param limit the maximum number of processes to retrieve
     * @return the list of fully completed processes
     */
    List<ProcessExecution> findAllFullyCompleted(int limit);

    /**
     * Retrieves all processes with pagination support.
     *
     * @param pageable the pagination parameters
     * @return pageable data containing processes and total count
     */
    PageableData<Process> findAll(Pageable pageable);

}
