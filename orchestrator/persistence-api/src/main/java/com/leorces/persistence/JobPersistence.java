package com.leorces.persistence;

import com.leorces.model.job.Job;
import com.leorces.model.pagination.Pageable;
import com.leorces.model.pagination.PageableData;

import java.util.Optional;

public interface JobPersistence {

    Job create(Job job);

    Job run(Job job);

    Job complete(Job job);

    Job fail(Job job);

    PageableData<Job> findAll(Pageable pageable);

    Optional<Job> findJobById(String jobId);

}
