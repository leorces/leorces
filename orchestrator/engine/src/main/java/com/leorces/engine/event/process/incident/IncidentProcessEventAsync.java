package com.leorces.engine.event.process.incident;


import com.leorces.engine.event.process.ProcessEvent;
import com.leorces.model.runtime.process.Process;


public class IncidentProcessEventAsync extends ProcessEvent {

    public final Process process;

    public IncidentProcessEventAsync(Process process) {
        super(process);
        this.process = process;
    }

}
