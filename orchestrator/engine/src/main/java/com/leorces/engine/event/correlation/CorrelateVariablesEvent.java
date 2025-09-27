package com.leorces.engine.event.correlation;


import com.leorces.model.runtime.process.Process;
import com.leorces.model.runtime.variable.Variable;

import java.util.List;


public class CorrelateVariablesEvent extends CorrelationEvent {

    public final Process process;
    public final List<Variable> variables;

    public CorrelateVariablesEvent(Process process, List<Variable> variables, Object source) {
        super(source);
        this.process = process;
        this.variables = variables;
    }

}
