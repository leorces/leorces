package com.leorces.persistence.postgres.utils;

import com.leorces.model.runtime.activity.ActivityExecution;
import com.leorces.model.runtime.activity.ActivityState;

import java.time.LocalDateTime;

public abstract class ActivityStateTransition {

    public static ActivityStateTransition to(ActivityState targetState) {
        return switch (targetState) {
            case SCHEDULED -> new CreateTransition();
            case ACTIVE -> new ActiveTransition();
            case COMPLETED -> new CompletedTransition();
            case TERMINATED -> new TerminatedTransition();
            case FAILED -> new FailedTransition();
        };
    }

    public abstract ActivityExecution apply(ActivityExecution activity);

    private static class CreateTransition extends ActivityStateTransition {

        @Override
        public ActivityExecution apply(ActivityExecution activity) {
            var now = LocalDateTime.now();
            return activity.toBuilder()
                    .state(ActivityState.SCHEDULED)
                    .createdAt(now)
                    .updatedAt(now)
                    .build();
        }

    }

    private static class ActiveTransition extends ActivityStateTransition {

        @Override
        public ActivityExecution apply(ActivityExecution activity) {
            var now = LocalDateTime.now();
            return activity.toBuilder()
                    .state(ActivityState.ACTIVE)
                    .createdAt(now)
                    .updatedAt(now)
                    .startedAt(now)
                    .build();
        }

    }

    private static class CompletedTransition extends ActivityStateTransition {

        @Override
        public ActivityExecution apply(ActivityExecution activity) {
            var now = LocalDateTime.now();
            return activity.toBuilder()
                    .state(ActivityState.COMPLETED)
                    .createdAt(activity.createdAt() != null ? activity.createdAt() : now)
                    .updatedAt(now)
                    .startedAt(activity.startedAt() != null ? activity.startedAt() : now)
                    .completedAt(now)
                    .build();
        }

    }

    private static class TerminatedTransition extends ActivityStateTransition {

        @Override
        public ActivityExecution apply(ActivityExecution activity) {
            return activity.toBuilder()
                    .state(ActivityState.TERMINATED)
                    .completedAt(LocalDateTime.now())
                    .build();
        }

    }

    private static class FailedTransition extends ActivityStateTransition {

        @Override
        public ActivityExecution apply(ActivityExecution activity) {
            var now = LocalDateTime.now();
            return activity.toBuilder()
                    .state(ActivityState.FAILED)
                    .createdAt(activity.createdAt() != null ? activity.createdAt() : now)
                    .updatedAt(now)
                    .startedAt(activity.startedAt() != null ? activity.startedAt() : now)
                    .completedAt(now)
                    .build();
        }

    }

}