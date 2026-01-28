package com.leorces.persistence.postgres;

import com.leorces.model.job.Job;
import com.leorces.model.job.JobState;
import com.leorces.model.pagination.Pageable;
import com.leorces.model.pagination.PageableData;
import com.leorces.persistence.JobPersistence;
import com.leorces.persistence.postgres.mapper.JobMapper;
import com.leorces.persistence.postgres.repository.JobRepository;
import com.leorces.persistence.postgres.utils.JobStateTransition;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
@AllArgsConstructor
public class JobPersistenceImpl implements JobPersistence {

    private final JobRepository jobRepository;
    private final JobMapper jobMapper;

    @Override
    public Job create(Job job) {
        log.debug("Create job: {}", job);
        return save(job, JobState.CREATED);
    }

    @Override
    public Job run(Job job) {
        log.debug("Run job: {}", job);
        return save(job, JobState.RUNNING);
    }

    @Override
    public Job complete(Job job) {
        log.debug("Complete job: {}", job);
        return save(job, JobState.COMPLETED);
    }

    @Override
    public Job fail(Job job) {
        log.debug("Fail job: {}", job);
        return save(job, JobState.FAILED);
    }

    @Override
    public PageableData<Job> findAll(Pageable pageable) {
        log.debug("Finding all jobs with pageable: {}", pageable);
        var result = jobRepository.findAll(pageable);
        return new PageableData<>(jobMapper.toJobs(result.data()), result.total());
    }

    @Override
    public Optional<Job> findJobById(String jobId) {
        log.debug("Finding job by id: {}", jobId);
        return jobRepository.findById(jobId)
                .map(jobMapper::toJob);
    }

    private Job save(Job job, JobState state) {
        return save(JobStateTransition.to(state).apply(job));
    }

    private Job save(Job job) {
        var entity = jobMapper.toEntity(job);
        var savedEntity = jobRepository.save(entity);
        return job.toBuilder()
                .id(savedEntity.getId())
                .build();
    }

}
