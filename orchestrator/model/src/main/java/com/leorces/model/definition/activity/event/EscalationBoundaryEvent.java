package com.leorces.model.definition.activity.event;


import com.leorces.model.definition.activity.ActivityType;
import com.leorces.model.definition.activity.BoundaryEventDefinition;
import lombok.Builder;

import java.util.List;
import java.util.Map;


@Builder(toBuilder = true)
public record EscalationBoundaryEvent(
        String id,
        String parentId,
        String name,
        String attachedToRef,
        boolean cancelActivity,
        String escalationCode,
        ActivityType type,
        List<String> incoming,
        List<String> outgoing,
        Map<String, Object> inputs,
        Map<String, Object> outputs
) implements BoundaryEventDefinition {

    @Override
    public ActivityType type() {
        return ActivityType.ESCALATION_BOUNDARY_EVENT;
    }

}