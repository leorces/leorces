package com.leorces.model.definition.activity.event.end;

import com.leorces.model.definition.activity.ActivityType;
import com.leorces.model.definition.activity.EscalationActivityDefinition;
import lombok.Builder;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Builder
public record EscalationEndEvent(
        String id,
        String parentId,
        String name,
        ActivityType type,
        List<String> incoming,
        List<String> outgoing,
        String escalationCode
) implements EscalationActivityDefinition {

    @Override
    public ActivityType type() {
        return ActivityType.ESCALATION_END_EVENT;
    }

    @Override
    public Map<String, Object> inputs() {
        return Collections.emptyMap();
    }

    @Override
    public Map<String, Object> outputs() {
        return Collections.emptyMap();
    }

}