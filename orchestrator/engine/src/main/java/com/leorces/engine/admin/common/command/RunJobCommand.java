package com.leorces.engine.admin.common.command;

import com.leorces.engine.core.ExecutionCommand;

import java.util.Map;

public record RunJobCommand(
        String jobType,
        Map<String, Object> input
) implements ExecutionCommand {

    public static RunJobCommand of(String jobType,
                                   Map<String, Object> input) {
        return new RunJobCommand(jobType, input);
    }

}
