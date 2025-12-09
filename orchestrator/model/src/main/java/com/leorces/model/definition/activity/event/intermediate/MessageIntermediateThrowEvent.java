package com.leorces.model.definition.activity.event.intermediate;

import com.leorces.model.definition.activity.ActivityType;
import com.leorces.model.definition.activity.ExternalTaskDefinition;
import com.leorces.model.definition.activity.MessageActivityDefinition;
import lombok.Builder;

import java.util.List;
import java.util.Map;

@Builder(toBuilder = true)
public record MessageIntermediateThrowEvent(
        String id,
        String parentId,
        String name,
        String topic,
        Integer retries,
        String timeout,
        String messageReference,
        ActivityType type,
        List<String> incoming,
        List<String> outgoing,
        Map<String, Object> inputs,
        Map<String, Object> outputs
) implements MessageActivityDefinition, ExternalTaskDefinition {

    @Override
    public ActivityType type() {
        return ActivityType.MESSAGE_INTERMEDIATE_THROW_EVENT;
    }

}
