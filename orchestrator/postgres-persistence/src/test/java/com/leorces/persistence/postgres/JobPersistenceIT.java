package com.leorces.persistence.postgres;

import com.leorces.model.job.Job;
import com.leorces.model.job.JobState;
import com.leorces.model.pagination.Pageable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Job Persistence Integration Tests")
class JobPersistenceIT extends RepositoryIT {

    private static final String JOB_TYPE = "TEST_JOB";

    @Test
    @DisplayName("Should create job successfully")
    void create() {
        // Given
        var job = createTestJob();

        // When
        var createdJob = jobPersistence.create(job);

        // Then
        assertThat(createdJob.id()).isNotNull();
        var foundJob = jobRepository.findById(createdJob.id());
        assertThat(foundJob).isPresent();
        assertThat(foundJob.get().getState()).isEqualTo(JobState.CREATED.name());
        assertThat(foundJob.get().getType()).isEqualTo(JOB_TYPE);
    }

    @Test
    @DisplayName("Should run job successfully")
    void run() {
        // Given
        var job = createTestJob();

        // When
        var runningJob = jobPersistence.run(job);

        // Then
        assertThat(runningJob.id()).isNotNull();
        var foundJob = jobRepository.findById(runningJob.id());
        assertThat(foundJob).isPresent();
        assertThat(foundJob.get().getState()).isEqualTo(JobState.RUNNING.name());
        assertThat(foundJob.get().getStartedAt()).isNotNull();
    }

    @Test
    @DisplayName("Should complete job successfully")
    void complete() {
        // Given
        var job = jobPersistence.run(createTestJob());

        // When
        var completedJob = jobPersistence.complete(job);

        // Then
        var foundJob = jobRepository.findById(completedJob.id());
        assertThat(foundJob).isPresent();
        assertThat(foundJob.get().getState()).isEqualTo(JobState.COMPLETED.name());
        assertThat(foundJob.get().getCompletedAt()).isNotNull();
    }

    @Test
    @DisplayName("Should fail job successfully")
    void fail() {
        // Given
        var job = jobPersistence.run(createTestJob());
        var jobToFail = job.toBuilder()
                .failureReason("Error")
                .failureTrace("Stacktrace")
                .build();

        // When
        var failedJob = jobPersistence.fail(jobToFail);

        // Then
        var foundJob = jobRepository.findById(failedJob.id());
        assertThat(foundJob).isPresent();
        assertThat(foundJob.get().getState()).isEqualTo(JobState.FAILED.name());
        assertThat(foundJob.get().getCompletedAt()).isNotNull();
    }

    @Test
    @DisplayName("Should find job by ID")
    void findById() {
        // Given
        var job = jobPersistence.create(createTestJob());

        // When
        var foundJob = jobPersistence.findJobById(job.id());

        // Then
        assertThat(foundJob).isPresent();
        assertThat(foundJob.get().id()).isEqualTo(job.id());
    }

    @Test
    @DisplayName("Should find all jobs with pagination")
    void findAll() {
        // Given
        jobPersistence.create(createTestJob());
        jobPersistence.create(createTestJob());
        var pageable = Pageable.builder()
                .offset(0L)
                .limit(10)
                .build();

        // When
        var result = jobPersistence.findAll(pageable);

        // Then
        assertThat(result.total()).isPositive();
        assertThat(result.data()).isNotEmpty();
    }

    private Job createTestJob() {
        return Job.builder()
                .type(JOB_TYPE)
                .input(Map.of())
                .build();
    }

}
