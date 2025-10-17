package com.leorces.extension.camunda.configuration.properties;


import org.springframework.boot.context.properties.bind.DefaultValue;

import java.util.Map;


public record ProcessProperties(
        @DefaultValue("0") int taskRetries,
        @DefaultValue("1h") String taskTimeout,
        Map<String, TasksProperties> tasks
) {

}
