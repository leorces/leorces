package com.leorces.api;


import com.leorces.model.pagination.Pageable;
import com.leorces.model.pagination.PageableData;
import com.leorces.model.runtime.process.ProcessExecution;


/**
 * Service for accessing historical process execution data.
 * Provides functionality to query completed and ongoing process executions.
 */
public interface HistoryService {

    /**
     * Retrieves all process executions with pagination support.
     *
     * @param pageable the pagination parameters
     * @return pageable data containing process executions
     */
    PageableData<ProcessExecution> findAll(Pageable pageable);

}
