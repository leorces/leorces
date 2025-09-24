package com.leorces.api;


import com.leorces.model.pagination.Pageable;
import com.leorces.model.pagination.PageableData;
import com.leorces.model.runtime.process.Process;
import com.leorces.model.runtime.process.ProcessExecution;

import java.util.Optional;


/**
 * Service for managing process instances and executions.
 * Provides functionality to query and retrieve process information.
 */
public interface ProcessService {

    /**
     * Retrieves all processes with pagination support.
     *
     * @param pageable the pagination parameters
     * @return pageable data containing processes
     */
    PageableData<Process> findAll(Pageable pageable);

    /**
     * Finds a process execution by its unique identifier.
     *
     * @param processId the unique identifier of the process
     * @return an Optional containing the process execution if found, empty otherwise
     */
    Optional<ProcessExecution> findById(String processId);

}
