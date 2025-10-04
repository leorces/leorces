package com.leorces.model.runtime.activity;


public enum ActivityState {
    SCHEDULED,
    ACTIVE,
    COMPLETED,
    TERMINATED,
    FAILED;

    public boolean isTerminal() {
        return this == TERMINATED || this == COMPLETED;
    }
}
