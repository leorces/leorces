package com.leorces.model.definition;


import lombok.Builder;


@Builder
public record ProcessDefinitionMetadata(
        String schema,
        String origin,
        String deployment
) {

}
