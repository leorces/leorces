package com.leorces.engine.exception.process;

public class ProcessNotFoundException extends RuntimeException {

    public ProcessNotFoundException() {
        super("Process not found");
    }


    public ProcessNotFoundException(String message) {
        super(message);
    }

}
