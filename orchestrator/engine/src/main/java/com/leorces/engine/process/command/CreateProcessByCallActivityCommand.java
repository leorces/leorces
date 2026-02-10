package com.leorces.engine.process.command;

import com.leorces.engine.core.ExecutionResultCommand;
import com.leorces.model.runtime.activity.ActivityExecution;
import com.leorces.model.runtime.process.Process;
import lombok.Builder;

import java.util.Map;

@Builder
public record CreateProcessByCallActivityCommand(
        ActivityExecution callActivity,
        Map<String, Object> additionalVariables
) implements ExecutionResultCommand<Process> {

    public static CreateProcessByCallActivityCommand of(ActivityExecution callActivity) {
        return CreateProcessByCallActivityCommand.builder().callActivity(callActivity).build();
    }

    public static CreateProcessByCallActivityCommand of(ActivityExecution callActivity,
                                                        Map<String, Object> additionalVariables) {
        return CreateProcessByCallActivityCommand.builder()
                .callActivity(callActivity)
                .additionalVariables(additionalVariables)
                .build();
    }

}
