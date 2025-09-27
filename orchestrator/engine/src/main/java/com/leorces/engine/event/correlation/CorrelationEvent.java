package com.leorces.engine.event.correlation;


import com.leorces.model.runtime.activity.ActivityExecution;
import com.leorces.model.runtime.process.Process;
import com.leorces.model.runtime.variable.Variable;
import org.springframework.context.ApplicationEvent;

import java.util.List;
import java.util.Map;


public class CorrelationEvent extends ApplicationEvent {

    public CorrelationEvent(Object source) {
        super(source);
    }


    public static CorrelateMessageEvent message(String messageName,
                                                String businessKey,
                                                Map<String, Object> correlationKeys,
                                                Map<String, Object> processVariables) {
        return new CorrelateMessageEvent(messageName, businessKey, correlationKeys, processVariables);
    }

    public static CorrelateVariablesEvent variables(Process process, List<Variable> variables) {
        return new CorrelateVariablesEvent(process, variables, process);
    }

    public static CorrelateErrorEvent error(ActivityExecution errorEndActivity) {
        return new CorrelateErrorEvent(errorEndActivity);
    }

}
