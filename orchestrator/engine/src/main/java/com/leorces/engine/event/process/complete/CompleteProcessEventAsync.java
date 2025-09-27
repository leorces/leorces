package com.leorces.engine.event.process.complete;


import com.leorces.engine.event.process.ProcessEvent;
import com.leorces.model.runtime.process.Process;


public class CompleteProcessEventAsync extends ProcessEvent {

    public final Process process;

    public CompleteProcessEventAsync(Process process) {
        super(process);
        this.process = process;
    }

}
