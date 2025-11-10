package com.leorces.model.definition.activity.event.end;

import com.leorces.model.definition.activity.ActivityDefinition;
import com.leorces.model.definition.activity.ActivityType;
import lombok.Builder;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Builder(toBuilder = true)
public record TerminateEndEvent(
        String id,
        String parentId,
        String name,
        ActivityType type,
        List<String> incoming,
        List<String> outgoing
) implements ActivityDefinition {

    @Override
    public ActivityType type() {
        return ActivityType.TERMINATE_END_EVENT;
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
