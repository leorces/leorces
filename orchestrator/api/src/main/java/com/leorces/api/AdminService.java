package com.leorces.api;

import com.leorces.model.job.Job;
import com.leorces.model.job.migration.ProcessMigrationPlan;
import com.leorces.model.pagination.Pageable;
import com.leorces.model.pagination.PageableData;

import java.util.Map;
import java.util.Optional;

/**
 * Service providing administrative operations for jobs and process management.
 * <p>
 * This service handles tasks such as running background jobs, querying job executions,
 * and generating process migration plans. It is intended for use by administrators
 * or system maintenance routines.
 */
public interface AdminService {

    /**
     * Executes a background job of the specified type with the provided input parameters.
     *
     * @param jobType the type or name of the job to execute
     * @param inputs  a map of input parameters required by the job
     */
    void runJob(String jobType, Map<String, Object> inputs);

    /**
     * Retrieves a paginated list of all job executions.
     *
     * @param pageable pagination parameters including page number and size
     * @return a {@link PageableData} containing the requested page of jobs
     */
    PageableData<Job> findAllJobs(Pageable pageable);

    /**
     * Finds a job by its unique identifier.
     *
     * @param jobId the unique identifier of the job
     * @return an {@link Optional} containing the job if found, or empty if not found
     */
    Optional<Job> findJobById(String jobId);

    /**
     * Generates a migration plan for a process instance based on the given migration parameters.
     * <p>
     * The migration plan details the steps required to move a process instance
     * from its current version to a target version, including any necessary task mappings
     * or transformation rules.
     *
     * @param migration the migration parameters describing source and target versions
     * @return a {@link ProcessMigrationPlan} detailing the steps for migration
     */
    ProcessMigrationPlan generateMigrationPlan(ProcessMigrationPlan migration);

}
