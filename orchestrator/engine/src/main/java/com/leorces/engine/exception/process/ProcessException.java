package com.leorces.engine.exception.process;

import com.leorces.model.search.ProcessFilter;

public class ProcessException extends RuntimeException {

    public ProcessException(String message) {
        super(message);
    }

    public static ProcessException moreThenOneProcessesFound(ProcessFilter filter) {
        return new ProcessException("More than one process found for filter: " + filter);
    }

}
