package com.leorces.model.definition.activity.event.intermediate;

import com.leorces.model.definition.activity.ActivityType;
import com.leorces.model.definition.activity.EscalationActivityDefinition;
import lombok.Builder;

import java.util.List;
import java.util.Map;

@Builder(toBuilder = true)
public record EscalationIntermediateThrowEvent(
        String id,
        String parentId,
        String name,
        String escalationCode,
        ActivityType type,
        List<String> incoming,
        List<String> outgoing,
        Map<String, Object> inputs,
        Map<String, Object> outputs
) implements EscalationActivityDefinition {

    @Override
    public ActivityType type() {
        return ActivityType.ESCALATION_INTERMEDIATE_THROW_EVENT;
    }

}
