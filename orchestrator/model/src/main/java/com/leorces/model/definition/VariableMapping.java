package com.leorces.model.definition;


import lombok.Builder;


@Builder(toBuilder = true)
public record VariableMapping(
        String source,
        String target,
        String sourceExpression,
        String expression,
        String variables,
        String businessKey,
        boolean local
) {

}