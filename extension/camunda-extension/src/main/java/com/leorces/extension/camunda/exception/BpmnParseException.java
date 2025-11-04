package com.leorces.extension.camunda.exception;

public class BpmnParseException extends RuntimeException {

    public BpmnParseException(String message) {
        super(message);
    }

    public BpmnParseException(String message, Throwable cause) {
        super(message, cause);
    }

}