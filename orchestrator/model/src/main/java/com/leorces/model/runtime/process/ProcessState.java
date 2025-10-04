package com.leorces.model.runtime.process;


public enum ProcessState {
    ACTIVE,
    COMPLETED,
    TERMINATED,
    INCIDENT;

    public boolean isTerminal() {
        return this == TERMINATED || this == COMPLETED;
    }
}
