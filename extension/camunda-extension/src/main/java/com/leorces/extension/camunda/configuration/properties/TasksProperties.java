package com.leorces.extension.camunda.configuration.properties;


import org.springframework.boot.context.properties.bind.DefaultValue;


public record TasksProperties(
        @DefaultValue("0") int retries
) {

}
