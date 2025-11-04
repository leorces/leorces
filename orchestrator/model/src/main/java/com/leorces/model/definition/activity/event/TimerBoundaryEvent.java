package com.leorces.model.definition.activity.event;

import com.leorces.model.definition.activity.ActivityType;
import com.leorces.model.definition.activity.BoundaryEventDefinition;
import lombok.Builder;

import java.util.List;
import java.util.Map;

@Builder(toBuilder = true)
public record TimerBoundaryEvent(
        String id,
        String parentId,
        String name,
        String attachedToRef,
        boolean cancelActivity,
        String timeDuration,
        String timeDate,
        String timeCycle,
        ActivityType type,
        List<String> incoming,
        List<String> outgoing,
        Map<String, Object> inputs,
        Map<String, Object> outputs
) implements BoundaryEventDefinition {

    @Override
    public ActivityType type() {
        return ActivityType.TIMER_BOUNDARY_EVENT;
    }

}