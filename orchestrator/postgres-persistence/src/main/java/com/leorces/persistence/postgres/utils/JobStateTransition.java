package com.leorces.persistence.postgres.utils;

import com.leorces.model.job.Job;
import com.leorces.model.job.JobState;

import java.time.LocalDateTime;

public abstract class JobStateTransition {

    public static JobStateTransition to(JobState targetState) {
        return switch (targetState) {
            case CREATED -> new CreatedTransition();
            case RUNNING -> new RunningTransition();
            case COMPLETED -> new CompletedTransition();
            case FAILED -> new FailedTransition();
        };
    }

    public abstract Job apply(Job job);

    private static class CreatedTransition extends JobStateTransition {
        @Override
        public Job apply(Job job) {
            var now = LocalDateTime.now();
            return job.toBuilder()
                    .state(JobState.CREATED)
                    .createdAt(now)
                    .updatedAt(now)
                    .startedAt(null)
                    .completedAt(null)
                    .build();
        }

    }

    private static class RunningTransition extends JobStateTransition {
        @Override
        public Job apply(Job job) {
            var now = LocalDateTime.now();
            return job.toBuilder()
                    .state(JobState.RUNNING)
                    .createdAt(job.createdAt() != null ? job.createdAt() : now)
                    .updatedAt(now)
                    .startedAt(job.startedAt() != null ? job.startedAt() : now)
                    .build();
        }

    }

    private static class CompletedTransition extends JobStateTransition {
        @Override
        public Job apply(Job job) {
            var now = LocalDateTime.now();
            return job.toBuilder()
                    .state(JobState.COMPLETED)
                    .failureReason(null)
                    .failureTrace(null)
                    .updatedAt(now)
                    .startedAt(job.startedAt() != null ? job.startedAt() : now)
                    .completedAt(now)
                    .build();
        }

    }

    private static class FailedTransition extends JobStateTransition {
        @Override
        public Job apply(Job job) {
            var now = LocalDateTime.now();
            return job.toBuilder()
                    .state(JobState.FAILED)
                    .updatedAt(now)
                    .startedAt(job.startedAt() != null ? job.startedAt() : now)
                    .completedAt(now)
                    .build();
        }

    }

}
