package com.leorces.engine.event.process.terminate;


import com.leorces.engine.event.process.ProcessEvent;


public class TerminateProcessByIdEvent extends ProcessEvent {

    public final String processId;

    public TerminateProcessByIdEvent(String processId) {
        super(processId);
        this.processId = processId;
    }

}
