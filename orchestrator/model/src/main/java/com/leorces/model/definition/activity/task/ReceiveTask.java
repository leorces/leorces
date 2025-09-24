package com.leorces.model.definition.activity.task;


import com.leorces.model.definition.activity.ActivityType;
import com.leorces.model.definition.activity.MessageActivityDefinition;
import lombok.Builder;

import java.util.List;
import java.util.Map;


@Builder(toBuilder = true)
public record ReceiveTask(
        String id,
        String parentId,
        String name,
        ActivityType type,
        List<String> incoming,
        List<String> outgoing,
        Map<String, Object> inputs,
        Map<String, Object> outputs,
        String messageReference
) implements MessageActivityDefinition {

    @Override
    public ActivityType type() {
        return ActivityType.RECEIVE_TASK;
    }

}
