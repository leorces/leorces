package com.leorces.engine.event.process;


import com.leorces.engine.event.process.cancel.CancelProcessByIdEvent;
import com.leorces.engine.event.process.complete.CompleteProcessEventAsync;
import com.leorces.engine.event.process.incident.IncidentProcessEventAsync;
import com.leorces.engine.event.process.start.StartProcessByCallActivityEvent;
import com.leorces.engine.event.process.terminate.TerminateProcessByIdEvent;
import com.leorces.model.runtime.activity.ActivityExecution;
import com.leorces.model.runtime.process.Process;
import org.springframework.context.ApplicationEvent;


public class ProcessEvent extends ApplicationEvent {

    public ProcessEvent(Object source) {
        super(source);
    }

    public static StartProcessByCallActivityEvent startByCallActivity(ActivityExecution activity) {
        return new StartProcessByCallActivityEvent(activity);
    }

    public static CompleteProcessEventAsync completeAsync(Process process) {
        return new CompleteProcessEventAsync(process);
    }

    public static IncidentProcessEventAsync incidentAsync(Process process) {
        return new IncidentProcessEventAsync(process);
    }

    public static CancelProcessByIdEvent cancelByIdEvent(String processId) {
        return new CancelProcessByIdEvent(processId);
    }

    public static TerminateProcessByIdEvent terminateByIdEvent(String processId) {
        return new TerminateProcessByIdEvent(processId);
    }

}
