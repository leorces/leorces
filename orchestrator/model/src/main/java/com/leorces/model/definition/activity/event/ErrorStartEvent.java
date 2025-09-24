package com.leorces.model.definition.activity.event;


import com.leorces.model.definition.activity.ActivityType;
import com.leorces.model.definition.activity.ErrorActivityDefinition;
import lombok.Builder;

import java.util.Collections;
import java.util.List;
import java.util.Map;


@Builder(toBuilder = true)
public record ErrorStartEvent(
        String id,
        String parentId,
        String name,
        ActivityType type,
        List<String> incoming,
        List<String> outgoing,
        String errorCode
) implements ErrorActivityDefinition {

    @Override
    public ActivityType type() {
        return ActivityType.ERROR_START_EVENT;
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
