package com.leorces.model.runtime.process;


public enum ProcessState {
    ACTIVE,
    COMPLETED,
    CANCELED,
    TERMINATED,
    INCIDENT;

    public boolean isTerminal() {
        return this == TERMINATED || this == CANCELED || this == COMPLETED;
    }
}
