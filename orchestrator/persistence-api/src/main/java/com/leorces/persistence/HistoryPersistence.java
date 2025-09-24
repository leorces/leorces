package com.leorces.persistence;


import com.leorces.model.pagination.Pageable;
import com.leorces.model.pagination.PageableData;
import com.leorces.model.runtime.process.ProcessExecution;

import java.util.List;


/**
 * Persistence layer for managing process execution history.
 * Provides operations for storing and retrieving historical process execution data.
 */
public interface HistoryPersistence {

    /**
     * Saves a list of process executions to the historical storage.
     *
     * @param processes the list of process executions to save in history
     */
    void save(List<ProcessExecution> processes);

    /**
     * Retrieves all historical process executions with pagination support.
     *
     * @param pageable the pagination parameters
     * @return pageable data containing historical process executions and total count
     */
    PageableData<ProcessExecution> findAll(Pageable pageable);

}
