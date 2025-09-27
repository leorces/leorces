package com.leorces.engine.event.process.cancel;


import com.leorces.engine.event.process.ProcessEvent;


public class CancelProcessByIdEvent extends ProcessEvent {

    public final String processId;

    public CancelProcessByIdEvent(String processId) {
        super(processId);
        this.processId = processId;
    }

}
