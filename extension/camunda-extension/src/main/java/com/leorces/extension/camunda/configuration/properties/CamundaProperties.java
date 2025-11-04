package com.leorces.extension.camunda.configuration.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "leorces.extension.camunda")
public record CamundaProperties(
        @DefaultValue("bpmn") String bpmnPath
) {

}