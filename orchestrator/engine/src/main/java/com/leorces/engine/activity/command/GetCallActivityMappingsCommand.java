package com.leorces.engine.activity.command;

import com.leorces.engine.core.ExecutionResultCommand;
import com.leorces.model.runtime.activity.ActivityExecution;
import lombok.Builder;

import java.util.Map;

@Builder
public record GetCallActivityMappingsCommand(
        ActivityExecution activity,
        Map<String, Object> variables,
        MappingType type
) implements ExecutionResultCommand<Map<String, Object>> {

    public static GetCallActivityMappingsCommand input(ActivityExecution activity, Map<String, Object> variables) {
        return GetCallActivityMappingsCommand.builder()
                .activity(activity)
                .variables(variables)
                .type(MappingType.INPUT)
                .build();
    }

    public static GetCallActivityMappingsCommand output(ActivityExecution activity) {
        return GetCallActivityMappingsCommand.builder()
                .activity(activity)
                .type(MappingType.OUTPUT)
                .build();
    }

    public enum MappingType {
        INPUT, OUTPUT
    }

}
