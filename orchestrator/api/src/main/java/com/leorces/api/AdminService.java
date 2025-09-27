package com.leorces.api;

/**
 * Administrative service providing database maintenance and optimization operations.
 * This service handles background tasks related to data lifecycle management,
 * including compaction of completed processes to optimize storage and performance.
 */
public interface AdminService {

    /**
     * Performs database compaction to optimize storage and performance.
     * This operation may clean up obsolete data and reorganize database structures.
     */
    void doCompaction();

}
