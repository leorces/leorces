package com.leorces.model.definition.attribute;

import lombok.Builder;

@Builder(toBuilder = true)
public record MultiInstanceLoopCharacteristics(
        String collection,
        String elementVariable,
        boolean isSequential
) {
}
