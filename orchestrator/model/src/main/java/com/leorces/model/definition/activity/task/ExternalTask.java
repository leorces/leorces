package com.leorces.model.definition.activity.task;


import com.leorces.model.definition.activity.ActivityDefinition;
import com.leorces.model.definition.activity.ActivityType;
import lombok.Builder;

import java.util.List;
import java.util.Map;


@Builder(toBuilder = true)
public record ExternalTask(
        String id,
        String parentId,
        String name,
        String topic,
        Integer retries,
        String timeout,
        ActivityType type,
        List<String> incoming,
        List<String> outgoing,
        Map<String, Object> inputs,
        Map<String, Object> outputs
) implements ActivityDefinition {

    @Override
    public ActivityType type() {
        return ActivityType.EXTERNAL_TASK;
    }

}
