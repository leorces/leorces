package com.leorces.model.definition.activity.gateway;

import com.leorces.model.definition.activity.ActivityDefinition;
import com.leorces.model.definition.activity.ActivityType;
import lombok.Builder;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Builder(toBuilder = true)
public record InclusiveGateway(
        String id,
        String parentId,
        String name,
        ActivityType type,
        List<String> incoming,
        List<String> outgoing,
        Map<String, List<String>> condition
) implements ActivityDefinition {

    @Override
    public ActivityType type() {
        return ActivityType.INCLUSIVE_GATEWAY;
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
