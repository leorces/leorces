package com.leorces.model.definition.activity.event.boundary;

import com.leorces.model.definition.activity.ActivityType;
import com.leorces.model.definition.activity.BoundaryEventDefinition;
import com.leorces.model.definition.activity.MessageActivityDefinition;
import lombok.Builder;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Builder(toBuilder = true)
public record MessageBoundaryEvent(
        String id,
        String parentId,
        String name,
        String attachedToRef,
        boolean cancelActivity,
        String messageReference,
        ActivityType type,
        List<String> incoming,
        List<String> outgoing,
        Map<String, Object> inputs,
        Map<String, Object> outputs
) implements BoundaryEventDefinition, MessageActivityDefinition {

    @Override
    public ActivityType type() {
        return ActivityType.MESSAGE_BOUNDARY_EVENT;
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