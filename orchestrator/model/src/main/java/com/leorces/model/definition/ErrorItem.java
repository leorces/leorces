package com.leorces.model.definition;


import lombok.Builder;


@Builder
public record ErrorItem(
        String name,
        String errorCode,
        String message
) {

}
