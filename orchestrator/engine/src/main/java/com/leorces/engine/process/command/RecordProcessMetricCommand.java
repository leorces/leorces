package com.leorces.engine.process.command;

import com.leorces.engine.core.ExecutionCommand;
import com.leorces.model.runtime.process.Process;

public record RecordProcessMetricCommand(
        String metricName,
        Process process
) implements ExecutionCommand {

    public static RecordProcessMetricCommand of(String metricName,
                                                Process process) {
        return new RecordProcessMetricCommand(metricName, process);
    }

}
