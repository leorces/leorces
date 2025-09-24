package com.leorces.model.definition.activity.event;


import com.leorces.model.definition.activity.ActivityDefinition;
import com.leorces.model.definition.activity.ActivityType;
import lombok.Builder;

import java.util.Collections;
import java.util.List;
import java.util.Map;


@Builder
public record ErrorEndEvent(
        String id,
        String parentId,
        String name,
        ActivityType type,
        List<String> incoming,
        List<String> outgoing,
        String errorCode
) implements ActivityDefinition {

    @Override
    public ActivityType type() {
        return ActivityType.ERROR_END_EVENT;
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
