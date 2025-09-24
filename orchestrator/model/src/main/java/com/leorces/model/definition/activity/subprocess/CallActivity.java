package com.leorces.model.definition.activity.subprocess;


import com.leorces.model.definition.VariableMapping;
import com.leorces.model.definition.activity.ActivityDefinition;
import com.leorces.model.definition.activity.ActivityType;
import lombok.Builder;

import java.util.List;
import java.util.Map;


@Builder(toBuilder = true)
public record CallActivity(
        String id,
        String parentId,
        String name,
        String calledElement,
        Integer calledElementVersion,
        boolean inheritVariables,
        ActivityType type,
        List<String> incoming,
        List<String> outgoing,
        Map<String, Object> inputs,
        Map<String, Object> outputs,
        List<VariableMapping> inputMappings,
        List<VariableMapping> outputMappings
) implements ActivityDefinition {

    @Override
    public ActivityType type() {
        return ActivityType.CALL_ACTIVITY;
    }

}
