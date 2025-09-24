package com.leorces.api;


/**
 * Service for repository maintenance operations.
 * Provides functionality for database and storage optimization.
 */
public interface RepositoryService {

    /**
     * Performs database compaction to optimize storage and performance.
     * This operation may clean up obsolete data and reorganize database structures.
     */
    void doCompaction();

}
