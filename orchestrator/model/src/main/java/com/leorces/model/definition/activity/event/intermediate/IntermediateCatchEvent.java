package com.leorces.model.definition.activity.event.intermediate;

import com.leorces.model.definition.activity.ActivityType;
import com.leorces.model.definition.activity.ConditionalActivityDefinition;
import lombok.Builder;

import java.util.List;
import java.util.Map;

@Builder(toBuilder = true)
public record IntermediateCatchEvent(
        String id,
        String parentId,
        String name,
        String condition,
        String variableName,
        String variableEvents,
        ActivityType type,
        List<String> incoming,
        List<String> outgoing,
        Map<String, Object> inputs,
        Map<String, Object> outputs
) implements ConditionalActivityDefinition {

    @Override
    public ActivityType type() {
        return ActivityType.INTERMEDIATE_CATCH_EVENT;
    }

}
