package com.leorces.model.definition.activity.subprocess;

import com.leorces.model.definition.activity.ActivityDefinition;
import com.leorces.model.definition.activity.ActivityType;
import lombok.Builder;

import java.util.List;
import java.util.Map;

@Builder(toBuilder = true)
public record Subprocess(
        String id,
        String parentId,
        String name,
        ActivityType type,
        List<String> incoming,
        List<String> outgoing,
        Map<String, Object> inputs,
        Map<String, Object> outputs
) implements ActivityDefinition {

    @Override
    public ActivityType type() {
        return ActivityType.SUBPROCESS;
    }

}
