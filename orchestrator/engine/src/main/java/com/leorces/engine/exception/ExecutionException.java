package com.leorces.engine.exception;

/**
 * Exception thrown when command execution fails.
 */
public class ExecutionException extends RuntimeException {

    public ExecutionException(String message) {
        super(message);
    }

    public ExecutionException(String message, Throwable cause) {
        super(message, cause);
    }

}