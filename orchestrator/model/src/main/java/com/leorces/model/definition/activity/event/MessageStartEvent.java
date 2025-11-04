package com.leorces.model.definition.activity.event;

import com.leorces.model.definition.activity.ActivityType;
import com.leorces.model.definition.activity.MessageActivityDefinition;
import lombok.Builder;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Builder(toBuilder = true)
public record MessageStartEvent(
        String id,
        String parentId,
        String name,
        ActivityType type,
        List<String> incoming,
        List<String> outgoing,
        String messageReference
) implements MessageActivityDefinition {

    @Override
    public ActivityType type() {
        return ActivityType.MESSAGE_START_EVENT;
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

