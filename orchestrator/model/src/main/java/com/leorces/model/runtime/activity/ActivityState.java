package com.leorces.model.runtime.activity;


public enum ActivityState {
    SCHEDULED,
    ACTIVE,
    COMPLETED,
    CANCELED,
    TERMINATED,
    FAILED;

    public boolean isTerminal() {
        return this == TERMINATED || this == CANCELED || this == COMPLETED;
    }
}
