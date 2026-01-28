package com.leorces.engine.job.common.handler;

import com.leorces.engine.core.CommandHandler;
import com.leorces.engine.job.common.command.JobCommand;
import com.leorces.engine.job.common.model.JobType;
import com.leorces.model.job.Job;
import com.leorces.persistence.JobPersistence;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.util.Map;

@Slf4j
@RequiredArgsConstructor
public abstract class AbstractJobHandler<C extends JobCommand> implements CommandHandler<C> {

    protected final JobPersistence jobPersistence;

    @Override
    public void handle(C command) {
        var input = command.input();
        var jobType = getJobType();

        log.info("Starting {} job with input: {}", jobType, input);
        var job = runJob(jobType, input);

        try {
            var output = execute(job, command);
            completeJob(job, output);
            log.info("Job finished: {}", job);
        } catch (Exception e) {
            log.error("Failed job: {}", job, e);
            failJob(job, e);
        }
    }

    protected abstract Map<String, Object> execute(Job job, C command);

    protected abstract JobType getJobType();

    private Job runJob(JobType jobType, Map<String, Object> input) {
        return jobPersistence.run(
                Job.builder()
                        .type(jobType.toString())
                        .input(input)
                        .build());
    }

    private void completeJob(Job job, Map<String, Object> output) {
        jobPersistence.complete(
                job.toBuilder()
                        .output(output)
                        .build()
        );
    }

    private void failJob(Job job, Exception e) {
        jobPersistence.fail(
                job.toBuilder()
                        .failureReason(e.getMessage())
                        .failureTrace(ExceptionUtils.getStackTrace(e))
                        .build()
        );
    }

}
