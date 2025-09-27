package com.leorces.engine.event.correlation;


import java.util.Map;


public class CorrelateMessageEvent extends CorrelationEvent {

    public String messageName;
    public String businessKey;
    public Map<String, Object> correlationKeys;
    public Map<String, Object> processVariables;

    public CorrelateMessageEvent(String messageName,
                                 String businessKey,
                                 Map<String, Object> correlationKeys,
                                 Map<String, Object> processVariables) {
        super(messageName);
        this.messageName = messageName;
        this.businessKey = businessKey;
        this.correlationKeys = correlationKeys;
        this.processVariables = processVariables;
    }

}
