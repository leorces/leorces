package com.leorces.model.definition.activity.event;

import com.leorces.model.definition.activity.ActivityType;
import com.leorces.model.definition.activity.BoundaryEventDefinition;
import com.leorces.model.definition.activity.ConditionalActivityDefinition;
import lombok.Builder;

import java.util.List;
import java.util.Map;

@Builder(toBuilder = true)
public record ConditionalBoundaryEvent(
        String id,
        String parentId,
        String name,
        String attachedToRef,
        boolean cancelActivity,
        String condition,
        ActivityType type,
        List<String> incoming,
        List<String> outgoing,
        Map<String, Object> inputs,
        Map<String, Object> outputs
) implements BoundaryEventDefinition, ConditionalActivityDefinition {

    @Override
    public ActivityType type() {
        return ActivityType.CONDITIONAL_BOUNDARY_EVENT;
    }

}