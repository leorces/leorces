package com.leorces.engine.exception.correlation;

public class MessageCorrelationException extends RuntimeException {

    private MessageCorrelationException(String message) {
        super(message);
    }

    public static MessageCorrelationException noProcessesCorrelated(String messageName) {
        return new MessageCorrelationException(
                "No process correlated with message: %s".formatted(messageName)
        );
    }

    public static MessageCorrelationException multipleProcessesCorrelated(String messageName) {
        return new MessageCorrelationException(
                "Found more than one process correlated with message: %s".formatted(messageName)
        );
    }

    public static MessageCorrelationException invalidProcessState(String processId) {
        return new MessageCorrelationException(
                "Process: %s is not in ACTIVE or INCIDENT state".formatted(processId)
        );
    }

    public static MessageCorrelationException missingBusinessKeyAndCorrelationKeys() {
        return new MessageCorrelationException(
                "Neither businessKey nor correlationKeys provided for message correlation"
        );
    }

}
