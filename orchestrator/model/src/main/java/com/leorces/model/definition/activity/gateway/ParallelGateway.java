package com.leorces.model.definition.activity.gateway;


import com.leorces.model.definition.activity.ActivityDefinition;
import com.leorces.model.definition.activity.ActivityType;
import lombok.Builder;

import java.util.Collections;
import java.util.List;
import java.util.Map;


@Builder(toBuilder = true)
public record ParallelGateway(
        String id,
        String parentId,
        String name,
        ActivityType type,
        List<String> incoming,
        List<String> outgoing
) implements ActivityDefinition {

    @Override
    public ActivityType type() {
        return ActivityType.PARALLEL_GATEWAY;
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
