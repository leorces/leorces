package com.leorces.persistence.postgres.utils;

import com.leorces.model.runtime.process.Process;
import com.leorces.model.runtime.process.ProcessState;

import java.time.LocalDateTime;

public abstract class ProcessStateTransition {

    public static ProcessStateTransition to(ProcessState targetState) {
        return switch (targetState) {
            case ACTIVE -> new ActiveTransition();
            case COMPLETED -> new CompletedTransition();
            case CANCELED -> new CanceledTransition();
            case TERMINATED -> new TerminatedTransition();
            case INCIDENT -> new IncidentTransition();
        };
    }

    public abstract Process apply(Process process);

    private static class ActiveTransition extends ProcessStateTransition {

        @Override
        public Process apply(Process process) {
            var now = LocalDateTime.now();
            var businessKey = process.businessKey() != null ? process.businessKey() : IdGenerator.getNewId();
            return process.toBuilder()
                    .businessKey(businessKey)
                    .state(ProcessState.ACTIVE)
                    .createdAt(process.createdAt() == null ? now : process.createdAt())
                    .updatedAt(now)
                    .startedAt(now)
                    .build();
        }

    }

    private static class CompletedTransition extends ProcessStateTransition {

        @Override
        public Process apply(Process process) {
            return process.toBuilder()
                    .state(ProcessState.COMPLETED)
                    .completedAt(LocalDateTime.now())
                    .build();
        }

    }

    private static class CanceledTransition extends ProcessStateTransition {

        @Override
        public Process apply(Process process) {
            return process.toBuilder()
                    .state(ProcessState.CANCELED)
                    .completedAt(LocalDateTime.now())
                    .build();
        }

    }

    private static class TerminatedTransition extends ProcessStateTransition {

        @Override
        public Process apply(Process process) {
            return process.toBuilder()
                    .state(ProcessState.TERMINATED)
                    .completedAt(LocalDateTime.now())
                    .build();
        }

    }

    private static class IncidentTransition extends ProcessStateTransition {


        @Override
        public Process apply(Process process) {
            var now = LocalDateTime.now();
            return process.toBuilder()
                    .state(ProcessState.INCIDENT)
                    .updatedAt(now)
                    .build();
        }

    }

}