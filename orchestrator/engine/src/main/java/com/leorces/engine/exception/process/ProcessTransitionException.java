package com.leorces.engine.exception.process;

import com.leorces.engine.exception.activity.ActivityTransitionException;
import com.leorces.model.runtime.process.Process;
import com.leorces.model.runtime.process.ProcessState;

public class ProcessTransitionException extends RuntimeException {

    public ProcessTransitionException(String message) {
        super(message);
    }


    public static ActivityTransitionException create(Process process, ProcessState toState) {
        return new ActivityTransitionException(
                "Process with id %s and definition id %s cannot transit from state: %s to state: %s"
                        .formatted(process.id(), process.definitionId(), process.state(), toState)
        );
    }

}
