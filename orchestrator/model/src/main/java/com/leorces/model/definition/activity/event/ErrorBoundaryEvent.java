package com.leorces.model.definition.activity.event;

import com.leorces.model.definition.activity.ActivityType;
import com.leorces.model.definition.activity.BoundaryEventDefinition;
import com.leorces.model.definition.activity.ErrorActivityDefinition;
import lombok.Builder;

import java.util.List;
import java.util.Map;

@Builder(toBuilder = true)
public record ErrorBoundaryEvent(
        String id,
        String parentId,
        String name,
        String attachedToRef,
        boolean cancelActivity,
        String errorCode,
        ActivityType type,
        List<String> incoming,
        List<String> outgoing,
        Map<String, Object> inputs,
        Map<String, Object> outputs
) implements BoundaryEventDefinition, ErrorActivityDefinition {

    @Override
    public ActivityType type() {
        return ActivityType.ERROR_BOUNDARY_EVENT;
    }

}