package com.leorces.model.definition.activity.subprocess;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.leorces.model.definition.VariableMapping;
import com.leorces.model.definition.activity.ActivityDefinition;
import com.leorces.model.definition.activity.ActivityType;
import com.leorces.model.definition.attribute.MultiInstanceLoopCharacteristics;
import lombok.Builder;

import java.util.List;
import java.util.Map;

@Builder(toBuilder = true)
public record CallActivity(
        String id,
        String parentId,
        String name,
        String calledElement,
        Integer calledElementVersion,
        ActivityType type,
        List<String> incoming,
        List<String> outgoing,
        Map<String, Object> inputs,
        Map<String, Object> outputs,
        List<VariableMapping> inputMappings,
        List<VariableMapping> outputMappings,
        MultiInstanceLoopCharacteristics multiInstanceLoopCharacteristics
) implements ActivityDefinition {

    @Override
    public ActivityType type() {
        return ActivityType.CALL_ACTIVITY;
    }

    @JsonIgnore
    public boolean shouldProcessAllInputMappings() {
        return shouldProcessAllMappings(inputMappings);
    }

    @JsonIgnore
    public boolean shouldProcessAllOutputMappings() {
        return shouldProcessAllMappings(outputMappings);
    }

    @JsonIgnore
    public boolean isMultiInstance() {
        return multiInstanceLoopCharacteristics() != null;
    }

    private boolean shouldProcessAllMappings(List<VariableMapping> mappings) {
        return mappings.stream()
                .anyMatch(mapping -> "all".equals(mapping.variables()));
    }

}

