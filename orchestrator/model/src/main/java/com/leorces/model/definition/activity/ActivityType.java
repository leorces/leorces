package com.leorces.model.definition.activity;

public enum ActivityType {
    // Task
    EXTERNAL_TASK,
    RECEIVE_TASK,

    // Gateway
    PARALLEL_GATEWAY,
    INCLUSIVE_GATEWAY,
    EXCLUSIVE_GATEWAY,
    EVENT_BASED_GATEWAY,

    // Events
    START_EVENT,
    MESSAGE_START_EVENT,
    END_EVENT,
    INTERMEDIATE_CATCH_EVENT,
    MESSAGE_INTERMEDIATE_CATCH_EVENT,
    ERROR_END_EVENT,
    ERROR_START_EVENT,
    TERMINATE_END_EVENT,
    ESCALATION_END_EVENT,
    ESCALATION_INTERMEDIATE_THROW_EVENT,
    ESCALATION_START_EVENT,

    // Boundary Events
    TIMER_BOUNDARY_EVENT,
    MESSAGE_BOUNDARY_EVENT,
    ERROR_BOUNDARY_EVENT,
    SIGNAL_BOUNDARY_EVENT,
    CONDITIONAL_BOUNDARY_EVENT,
    ESCALATION_BOUNDARY_EVENT,

    // SubProcess
    SUBPROCESS,
    EVENT_SUBPROCESS,
    CALL_ACTIVITY;

    public boolean isSubprocess() {
        return this == SUBPROCESS || this == EVENT_SUBPROCESS;
    }

    public boolean isEventSubprocess() {
        return this == EVENT_SUBPROCESS;
    }

    public boolean isStartEvent() {
        return this == START_EVENT
                || this == MESSAGE_START_EVENT
                || this == ERROR_START_EVENT
                || this == ESCALATION_START_EVENT;
    }

    public boolean isEscalationEndEvent() {
        return this == ESCALATION_END_EVENT;
    }
}
